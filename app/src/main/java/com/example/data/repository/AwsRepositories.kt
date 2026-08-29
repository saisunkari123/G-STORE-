package com.example.data.repository

import android.content.Context
import android.util.Log
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.core.Amplify
import com.example.domain.model.*
import com.example.domain.repository.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume

class JsonPersister(private val context: Context) {
    private val gson = Gson()

    fun <T> saveList(fileName: String, list: List<T>) {
        try {
            val json = gson.toJson(list)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("JsonPersister", "Error saving $fileName", e)
        }
    }

    fun <T> loadList(fileName: String, clazz: Class<T>): List<T> {
        try {
            val file = context.getFileStreamPath(fileName)
            if (!file.exists()) return emptyList()
            context.openFileInput(fileName).use { stream ->
                val json = stream.bufferedReader().use { it.readText() }
                val type = TypeToken.getParameterized(List::class.java, clazz).type
                return gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("JsonPersister", "Error loading $fileName", e)
            return emptyList()
        }
    }
}

class AwsProductRepositoryImpl(private val context: Context) : ProductRepository {
    private val persister = JsonPersister(context)
    private val gson = Gson()
    private val defaultCategories = listOf(
        Category(id = "c_rice", nameEn = "Rice Bags", imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop"),
        Category(id = "c_oil", nameEn = "Cooking Oils", imageUrl = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop"),
        Category(id = "c_dal", nameEn = "Dals & Pulses", imageUrl = "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop"),
        Category(id = "c_dairy", nameEn = "Dairy Essentials", imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop"),
        Category(id = "c_spices", nameEn = "Spices & Masalas", imageUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop"),
        Category(id = "c_dryfruits", nameEn = "Dry Fruits & Nuts", imageUrl = "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=600&auto=format&fit=crop"),
        Category(id = "c_snacks", nameEn = "Snacks & Namkeen", imageUrl = "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop"),
        Category(id = "c_biscuits", nameEn = "Biscuits & Bakery", imageUrl = "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&auto=format&fit=crop"),
        Category(id = "c_beverages", nameEn = "Tea, Coffee & Drinks", imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop"),
        Category(id = "c_cleaning", nameEn = "Home & Cleaning", imageUrl = "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=600&auto=format&fit=crop")
    )
    private val categoriesState = MutableStateFlow(
        run {
            val saved = persister.loadList("aws_categories.json", Category::class.java)
            if (saved.isEmpty()) {
                defaultCategories
            } else {
                val savedIds = saved.map { it.id }.toSet()
                val missingDefaults = defaultCategories.filter { it.id !in savedIds }
                saved + missingDefaults
            }
        }
    )
    private val giftConfigsState = MutableStateFlow(persister.loadList("aws_gifts.json", GiftItemConfig::class.java))
    private val productsState = MutableStateFlow(persister.loadList("aws_products.json", Product::class.java).ifEmpty { emptyList() })
    private val appConfigState = MutableStateFlow(run {
        // Load persisted AppConfig from local JSON; default to 150/10 if not found
        val saved = persister.loadList("aws_app_config.json", AppConfig::class.java)
        saved.firstOrNull() ?: AppConfig(minimumOrderAmount = 150.0, deliveryRadiusKm = 10.0)
    })
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Start background 5-second cloud sync so all devices always see fresh product, gift, and config data
        startPeriodicCloudSync()
    }

    /** Polls AWS AppSync every 5 seconds and updates the local state if data changed. */
    private fun startPeriodicCloudSync() {
        syncScope.launch {
            while (true) {
                delay(5_000L)
                try {
                    kotlinx.coroutines.withTimeout(15_000L) {
                        fetchProductsFromCloud()
                    }
                } catch (e: Exception) {
                    Log.w("AwsProduct", "Periodic sync failed silently", e)
                }
            }
        }
    }

    /** Immediately fetches products from AppSync and updates the Flow (called on catalog open). */
    override suspend fun forceRefreshFromCloud() {
        try {
            fetchProductsFromCloud()
        } catch (e: Exception) {
            Log.w("AwsProduct", "Force refresh failed silently", e)
        }
    }

    /**
     * Queries AppSync listProducts and completely replaces the local state.
     * Admin-created products on other devices will appear within 3s automatically.
     */
    private suspend fun fetchProductsFromCloud(): Unit = suspendCancellableCoroutine { cont ->
        val query = """
            query ListProducts_${System.currentTimeMillis()} {
                listProducts(limit: 1000) {
                    items {
                        id
                        name
                        category
                        description
                        imageUrls
                        variants {
                            size
                            price
                            stock
                        }
                        createdAt
                        updatedAt
                    }
                }
            }
        """.trimIndent()
        val request = SimpleGraphQLRequest<String>(
            query,
            emptyMap<String, Any>(),
            String::class.java,
            GsonVariablesSerializer()
        )
        Amplify.API.query(request,
            { response ->
                try {
                    val json = response.data
                    if (json != null) {
                        val gson = Gson()
                        val root = gson.fromJson(json, Map::class.java)
                        val listProducts = root["listProducts"] as? Map<*, *>
                        val items = listProducts?.get("items") as? List<*>
                        if (items != null) {
                            val cloudProducts = items.mapNotNull { item ->
                                try {
                                    val itemJson = gson.toJson(item)
                                    val raw = gson.fromJson(itemJson, Map::class.java)
                                    val id = raw["id"] as? String ?: ""
                                    val name = raw["name"] as? String ?: ""
                                    val category = raw["category"] as? String ?: ""
                                    val description = raw["description"] as? String ?: ""
                                    val imageUrlsRaw = raw["imageUrls"] as? List<*> ?: emptyList<Any>()
                                    val imageUrls = imageUrlsRaw.mapNotNull { it as? String }
                                    
                                    val variantsRaw = raw["variants"] as? List<*> ?: emptyList<Any>()
                                    val variants = variantsRaw.mapNotNull { v ->
                                        val vMap = v as? Map<*, *> ?: return@mapNotNull null
                                        val sizeStr = vMap["size"] as? String ?: ""
                                        val parts = sizeStr.split(":::")
                                        val weight = parts.getOrNull(0) ?: sizeStr
                                        val unit = parts.getOrNull(1) ?: "Kg"
                                        val mrp = parts.getOrNull(2)?.toDoubleOrNull() ?: ((vMap["price"] as? Number)?.toDouble() ?: 0.0)
                                        val sku = parts.getOrNull(3) ?: ""
                                        val vId = parts.getOrNull(4) ?: UUID.randomUUID().toString()
                                        val curPrice = (vMap["price"] as? Number)?.toDouble() ?: 0.0
                                        val stock = (vMap["stock"] as? Number)?.toInt() ?: 0
                                        ProductVariant(
                                            id = vId,
                                            weight = weight,
                                            unit = unit,
                                            currentPrice = curPrice,
                                            mrp = mrp,
                                            stockQuantity = stock,
                                            sku = sku
                                        )
                                    }

                                    val descParts = description.split(" ::: ")
                                    val brand = descParts.getOrNull(0) ?: ""
                                    val nameTe = descParts.getOrNull(1) ?: ""
                                    val shortDescEn = descParts.getOrNull(2) ?: ""
                                    val shortDescTe = descParts.getOrNull(3) ?: ""
                                    val fullDescEn = descParts.getOrNull(4) ?: description
                                    val fullDescTe = descParts.getOrNull(5) ?: ""
                                    val isEnabled = descParts.getOrNull(6)?.toBooleanStrictOrNull() ?: true

                                    Product(
                                        id = id,
                                        categoryId = category,
                                        nameEn = name,
                                        nameTe = nameTe,
                                        brand = brand,
                                        descriptionEn = fullDescEn,
                                        descriptionTe = fullDescTe,
                                        shortDescriptionEn = shortDescEn,
                                        shortDescriptionTe = shortDescTe,
                                        imageUrls = imageUrls,
                                        variants = variants,
                                        isEnabled = isEnabled,
                                        isAvailable = isEnabled
                                    )
                                } catch (e: Exception) {
                                    Log.w("AwsProduct", "Single product parse error", e)
                                    null
                                }
                             }
                             
                             // Look for the categories configuration record in the raw items list
                             val sysCatProd = cloudProducts.find { it.id == "sys_categories" }
                             if (sysCatProd != null) {
                                 try {
                                     val type = object : TypeToken<List<Category>>() {}.type
                                     val jsonStr = if (sysCatProd.descriptionEn.isNotBlank() && sysCatProd.descriptionEn != "System Categories") sysCatProd.descriptionEn else sysCatProd.nameEn
                                     val categories: List<Category> = gson.fromJson(jsonStr, type) ?: emptyList()
                                     if (categories.isNotEmpty()) {
                                         categoriesState.value = categories
                                         persister.saveList("aws_categories.json", categories)
                                     }
                                 } catch (e: Exception) {
                                     Log.e("AwsProduct", "Failed to parse categories from sys_categories", e)
                                 }
                             } else {
                                 // If database doesn't have sys_categories but we have categories cached locally
                                 if (categoriesState.value.isEmpty()) {
                                     categoriesState.value = defaultCategories
                                     persister.saveList("aws_categories.json", defaultCategories)
                                     syncScope.launch { saveCategories(defaultCategories) }
                                 } else {
                                     syncScope.launch { saveCategories(categoriesState.value) }
                                 }
                             }
                             
                             // Look for sys_gifts configuration record
                              val sysGiftsProd = cloudProducts.find { it.id == "sys_gifts" }
                              if (sysGiftsProd != null) {
                                  try {
                                      val type = object : TypeToken<List<GiftItemConfig>>() {}.type
                                      val jsonStr = if (sysGiftsProd.descriptionEn.isNotBlank() && sysGiftsProd.descriptionEn != "System Gifts") sysGiftsProd.descriptionEn else sysGiftsProd.nameEn
                                      val gifts: List<GiftItemConfig> = gson.fromJson(jsonStr, type) ?: emptyList()
                                      giftConfigsState.value = gifts
                                      persister.saveList("aws_gifts.json", gifts)
                                  } catch (e: Exception) {
                                      Log.e("AwsProduct", "Failed to parse gifts from sys_gifts", e)
                                  }
                              } else {
                                  if (giftConfigsState.value.isNotEmpty()) {
                                      syncScope.launch { saveGiftConfigs(giftConfigsState.value) }
                                  }
                              }

                              // Look for sys_config (admin store settings: minimum order, delivery radius)
                              val sysConfigProd = cloudProducts.find { it.id == "sys_config" }
                              if (sysConfigProd != null) {
                                  try {
                                      val jsonStr = if (sysConfigProd.descriptionEn.isNotBlank() && sysConfigProd.descriptionEn != "System App Config") sysConfigProd.descriptionEn else sysConfigProd.nameEn
                                      val config = gson.fromJson(jsonStr, AppConfig::class.java)
                                      if (config != null) {
                                          appConfigState.value = config
                                          persister.saveList("aws_app_config.json", listOf(config))
                                          Log.i("AwsProduct", "Cloud sync: Updated app config (deliveryRadiusKm = ${config.deliveryRadiusKm})")
                                      }
                                  } catch (e: Exception) {
                                      Log.e("AwsProduct", "Failed to parse app config from sys_config", e)
                                  }
                              }

                              // Filter all system records out of customer-facing products list
                              val actualProducts = cloudProducts.filter {
                                  it.id != "sys_categories" && it.id != "sys_gifts" && it.id != "sys_config"
                              }
                              if (actualProducts.isNotEmpty()) {
                                  val localOnly = productsState.value.filter { local -> actualProducts.none { cloud -> cloud.id == local.id } }
                                  val merged = actualProducts + localOnly
                                  productsState.value = merged
                                  persister.saveList("aws_products.json", merged)
                                  Log.i("AwsProduct", "Cloud sync: ${merged.size} total products (${actualProducts.size} from AppSync, ${localOnly.size} local)")
                              }
                         }
                    }
                } catch (e: Exception) {
                    Log.w("AwsProduct", "Cloud sync parse error", e)
                } finally {
                    cont.resume(Unit)
                }
            },
            { error ->
                Log.w("AwsProduct", "Cloud sync query failed", error)
                cont.resume(Unit)
            }
        )
    }

    override fun getAllCategories(): Flow<List<Category>> = categoriesState

    override suspend fun saveCategories(categories: List<Category>) {
        categoriesState.value = categories
        persister.saveList("aws_categories.json", categories)

        val sysProd = Product(
            id = "sys_categories",
            categoryId = "metadata",
            nameEn = "System Categories",
            brand = "System",
            descriptionEn = gson.toJson(categories),
            variants = emptyList(),
            isEnabled = false,
            isAvailable = false
        )
        try {
            saveProduct(sysProd)
        } catch (e: Exception) {
            Log.e("AwsProduct", "Failed to save categories metadata to cloud", e)
        }
    }

    override suspend fun clearCategories() {
        categoriesState.value = emptyList()
        persister.saveList("aws_categories.json", emptyList<Category>())
        try {
            deleteProduct("sys_categories")
        } catch (e: Exception) {
            Log.e("AwsProduct", "Failed to delete categories metadata from cloud", e)
        }
    }

    override fun getAppConfig(): Flow<AppConfig> = appConfigState

    override suspend fun saveAppConfig(config: AppConfig) {
        appConfigState.value = config
        persister.saveList("aws_app_config.json", listOf(config))
        // Persist to AppSync as sys_config product so all devices get it on next poll
        val sysProd = Product(
            id = "sys_config",
            categoryId = "metadata",
            nameEn = "System App Config",
            brand = "System",
            descriptionEn = gson.toJson(config),
            variants = emptyList(),
            isEnabled = false,
            isAvailable = false
        )
        try {
            saveProduct(sysProd)
        } catch (e: Exception) {
            Log.e("AwsProduct", "Failed to save app config to cloud", e)
        }
    }

    override fun getGiftConfigs(): Flow<List<GiftItemConfig>> = giftConfigsState

    override suspend fun saveGiftConfigs(configs: List<GiftItemConfig>) {
        giftConfigsState.value = configs
        persister.saveList("aws_gifts.json", configs)

        val sysProd = Product(
            id = "sys_gifts",
            categoryId = "metadata",
            nameEn = "System Gifts",
            brand = "System",
            descriptionEn = gson.toJson(configs),
            variants = emptyList(),
            isEnabled = false,
            isAvailable = false
        )
        try {
            saveProduct(sysProd)
        } catch (e: Exception) {
            Log.e("AwsProduct", "Failed to save gifts metadata to cloud", e)
        }
    }

    override suspend fun clearGiftConfigs() {
        giftConfigsState.value = emptyList()
        persister.saveList("aws_gifts.json", emptyList<GiftItemConfig>())
        try {
            deleteProduct("sys_gifts")
        } catch (e: Exception) {
            Log.e("AwsProduct", "Failed to delete gifts metadata from cloud", e)
        }
    }

    override fun getAllProducts(): Flow<List<Product>> = productsState

    override suspend fun getProductById(productId: String): Product? {
        return productsState.value.find { it.id == productId }
    }

    override suspend fun saveProducts(products: List<Product>) {
        productsState.value = products
        persister.saveList("aws_products.json", products)
    }

    override suspend fun saveProduct(product: Product) {
        val idToUse = product.id.ifEmpty { UUID.randomUUID().toString() }
        val prodWithId = product.copy(id = idToUse)

        if (prodWithId.id != "sys_categories" && prodWithId.id != "sys_gifts" && prodWithId.id != "sys_config") {
            val currentList = productsState.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == product.id }
            if (index != -1) {
                currentList[index] = prodWithId
            } else {
                currentList.add(prodWithId)
            }
            productsState.value = currentList
            persister.saveList("aws_products.json", currentList)
        }

        // Asynchronously sync to AWS AppSync Product Table
        try {
            val variantsInput = prodWithId.variants.map { v ->
                mapOf(
                    "size" to "${v.weight}:::${v.unit}:::${v.mrp}:::${v.sku}:::${v.id}",
                    "price" to v.currentPrice.toFloat(),
                    "stock" to v.stockQuantity
                )
            }
            val descPayload = if (prodWithId.id.startsWith("sys_")) {
                prodWithId.descriptionEn
            } else {
                "${prodWithId.brand} ::: ${prodWithId.nameTe} ::: ${prodWithId.shortDescriptionEn} ::: ${prodWithId.shortDescriptionTe} ::: ${prodWithId.descriptionEn} ::: ${prodWithId.descriptionTe} ::: ${prodWithId.isEnabled}"
            }
            val inputMap = mapOf(
                "id" to prodWithId.id,
                "name" to prodWithId.nameEn,
                "category" to prodWithId.categoryId,
                "description" to descPayload,
                "imageUrls" to prodWithId.imageUrls,
                "variants" to variantsInput
            )

            val isExistingInCloud = productsState.value.any { it.id == prodWithId.id } && prodWithId.id.startsWith("p_") && !prodWithId.id.startsWith("p_${System.currentTimeMillis().toString().take(6)}")

            val createMutation = """
                mutation CreateProduct(${"$"}input: CreateProductInput!) {
                    createProduct(input: ${"$"}input) {
                        id
                    }
                }
            """.trimIndent()

            val createRequest = SimpleGraphQLRequest<String>(
                createMutation,
                mapOf("input" to inputMap),
                String::class.java,
                GsonVariablesSerializer()
            )

            val updateMutation = """
                mutation UpdateProduct(${"$"}input: UpdateProductInput!) {
                    updateProduct(input: ${"$"}input) {
                        id
                    }
                }
            """.trimIndent()

            val updateRequest = SimpleGraphQLRequest<String>(
                updateMutation,
                mapOf("input" to inputMap),
                String::class.java,
                GsonVariablesSerializer()
            )

            if (!isExistingInCloud) {
                // Try CreateProduct directly for new products
                Amplify.API.mutate(createRequest,
                    { cRes -> 
                        if (cRes.data != null && !cRes.hasErrors() && !cRes.data.contains("\"createProduct\":null")) {
                            Log.i("AwsProduct", "Product created in AWS: ${cRes.data}")
                        } else {
                            // Fallback to update if already exists in cloud
                            Amplify.API.mutate(updateRequest,
                                { uRes -> Log.i("AwsProduct", "Product updated in AWS: ${uRes.data}") },
                                { uErr -> Log.e("AwsProduct", "AWS Product update fallback failed", uErr) }
                            )
                        }
                    },
                    { cErr -> 
                        // Fallback to update on error
                        Amplify.API.mutate(updateRequest,
                            { uRes -> Log.i("AwsProduct", "Product updated in AWS: ${uRes.data}") },
                            { uErr -> Log.e("AwsProduct", "AWS Product update fallback failed", uErr) }
                        )
                    }
                )
            } else {
                // Try UpdateProduct for existing products
                Amplify.API.mutate(updateRequest,
                    { response ->
                        if (response.data != null && !response.hasErrors() && !response.data.contains("\"updateProduct\":null")) {
                            Log.i("AwsProduct", "Product updated in AWS successfully: ${response.data}")
                        } else {
                            // Fallback to CreateProduct if not found in cloud
                            Amplify.API.mutate(createRequest,
                                { cRes -> Log.i("AwsProduct", "Product created in AWS: ${cRes.data}") },
                                { cErr -> Log.e("AwsProduct", "AWS Product create failed", cErr) }
                            )
                        }
                    },
                    { error ->
                        Amplify.API.mutate(createRequest,
                            { cRes -> Log.i("AwsProduct", "Product created in AWS: ${cRes.data}") },
                            { cErr -> Log.e("AwsProduct", "AWS Product create failed", cErr) }
                        )
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("AwsProduct", "AWS Product sync failed", e)
        }
    }

    override suspend fun deleteProduct(productId: String) {
        val currentList = productsState.value.toMutableList()
        currentList.removeAll { it.id == productId }
        productsState.value = currentList
        persister.saveList("aws_products.json", currentList)

        try {
            val mutation = """
                mutation DeleteProduct(${"$"}input: DeleteProductInput!) {
                    deleteProduct(input: ${"$"}input) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf("input" to mapOf("id" to productId)),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> 
                    Log.i("AwsProduct", "Product deleted on AWS successfully: ${response.data}")
                    syncScope.launch { forceRefreshFromCloud() }
                },
                { error -> Log.e("AwsProduct", "AWS Product delete failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsProduct", "AWS Product delete failed", e)
        }
    }
}

class AwsOrderRepositoryImpl(private val context: Context) : OrderRepository {
    private val persister = JsonPersister(context)
    private val gson = Gson()
    private val ordersState = MutableStateFlow(persister.loadList("aws_orders.json", Order::class.java))
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Start polling AppSync for new orders every 3 seconds.
        // This ensures admin sees new customer orders in real-time across devices.
        startPeriodicOrderSync()
    }

    override suspend fun forceRefreshFromCloud() {
        try {
            kotlinx.coroutines.withTimeout(15_000L) {
                fetchOrdersFromCloud()
            }
        } catch (e: Exception) {
            Log.w("AwsOrder", "Force refresh failed silently", e)
        }
    }

    /** Polls AppSync listOrders every 3 seconds and merges results into ordersState. */
    private fun startPeriodicOrderSync() {
        syncScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.withTimeout(15_000L) {
                        fetchOrdersFromCloud()
                    }
                } catch (e: Exception) {
                    Log.w("AwsOrder", "Periodic order sync failed silently", e)
                }
                delay(3_000L)
            }
        }
    }

    /**
     * Fetches all orders from AppSync and merges them with local state.
     * Remote wins on conflict (same id) — so admin sees the latest status from any device.
     */
    private suspend fun fetchOrdersFromCloud(): Unit = suspendCancellableCoroutine { cont ->
        val query = """
            query ListOrders_${System.currentTimeMillis()} {
                listOrders(limit: 1000) {
                    items {
                        id
                        customerId
                        customerName
                        deliveryAddress
                        deliveryFee
                        latitude
                        longitude
                        status
                        subtotal
                        total
                        createdAt
                        items {
                            productId
                            productName
                            quantity
                            price
                            variantSize
                        }
                    }
                }
            }
        """.trimIndent()
        val request = SimpleGraphQLRequest<String>(
            query,
            emptyMap<String, Any>(),
            String::class.java,
            GsonVariablesSerializer()
        )
        Amplify.API.query(request,
            { response ->
                try {
                    val json = response.data
                    if (json != null) {
                        val root = gson.fromJson(json, Map::class.java)
                        val listOrders = root["listOrders"] as? Map<*, *>
                        val items = listOrders?.get("items") as? List<*>
                        if (items != null) {
                            val remoteOrders = items.mapNotNull { item ->
                                try {
                                    val itemJson = gson.toJson(item)
                                    val raw = gson.fromJson(itemJson, Map::class.java)

                                    val rawAddr = raw["deliveryAddress"] as? String ?: ""
                                    val parts = rawAddr.split(" ::: ")
                                    val houseNo = parts.getOrNull(0) ?: rawAddr
                                    val landmark = parts.getOrNull(1) ?: ""
                                    val phone = parts.getOrNull(2) ?: ""
                                    val parsedDistKm = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0

                                    val itemsRaw = raw["items"] as? List<*> ?: emptyList<Any>()
                                    val orderItems = itemsRaw.mapNotNull { i ->
                                        val iMap = i as? Map<*, *> ?: return@mapNotNull null
                                        OrderItem(
                                            productId = iMap["productId"] as? String ?: "",
                                            productName = iMap["productName"] as? String ?: "",
                                            selectedSize = iMap["variantSize"] as? String ?: "",
                                            priceAtPurchase = (iMap["price"] as? Number)?.toDouble() ?: 0.0,
                                            quantity = (iMap["quantity"] as? Number)?.toInt() ?: 1
                                        )
                                    }

                                    val statusStr = raw["status"] as? String ?: "PENDING"
                                    val status = try { OrderStatus.valueOf(statusStr) } catch (_: Exception) { OrderStatus.PENDING }
                                    
                                    val rawCreatedAt = raw["createdAt"]
                                    val createdAtVal = when (rawCreatedAt) {
                                        is Number -> rawCreatedAt.toLong()
                                        is String -> {
                                            try {
                                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                                sdf.parse(rawCreatedAt)?.time ?: rawCreatedAt.toLongOrNull() ?: System.currentTimeMillis()
                                            } catch (_: Exception) {
                                                rawCreatedAt.toLongOrNull() ?: System.currentTimeMillis()
                                            }
                                        }
                                        else -> System.currentTimeMillis()
                                    }
                                    
                                    val subtotalVal = (raw["subtotal"] as? Number)?.toDouble() ?: (raw["subtotal"] as? String)?.toDoubleOrNull() ?: 0.0
                                    val deliveryFeeVal = (raw["deliveryFee"] as? Number)?.toDouble() ?: (raw["deliveryFee"] as? String)?.toDoubleOrNull() ?: 0.0
                                    val totalAmountVal = (raw["total"] as? Number)?.toDouble() ?: (raw["total"] as? String)?.toDoubleOrNull() ?: (subtotalVal + deliveryFeeVal)
                                    val latVal = (raw["latitude"] as? Number)?.toDouble() ?: (raw["latitude"] as? String)?.toDoubleOrNull() ?: 0.0
                                    val lngVal = (raw["longitude"] as? Number)?.toDouble() ?: (raw["longitude"] as? String)?.toDoubleOrNull() ?: 0.0

                                    Order(
                                        id = raw["id"] as? String ?: "",
                                        userId = raw["customerId"] as? String ?: "",
                                        customerName = raw["customerName"] as? String ?: "",
                                        customerPhone = phone,
                                        addressHouseNo = houseNo,
                                        addressLandmark = landmark,
                                        distanceKm = parsedDistKm,
                                        latitude = latVal,
                                        longitude = lngVal,
                                        subtotal = subtotalVal,
                                        deliveryFee = deliveryFeeVal,
                                        totalAmount = totalAmountVal,
                                        status = status,
                                        createdAt = createdAtVal,
                                        items = orderItems
                                    )
                                } catch (e: Exception) { 
                                    Log.w("AwsOrder", "Single order parse error", e)
                                    null 
                                }
                            }
                            // Merge: build map of local orders, overwrite/add remote orders
                            val mergedMap = ordersState.value.associateBy { it.id }.toMutableMap()
                            remoteOrders.forEach { mergedMap[it.id] = it }
                            val mergedList = mergedMap.values.toList()
                            ordersState.value = mergedList
                            persister.saveList("aws_orders.json", mergedList)
                            Log.i("AwsOrder", "Cloud sync: ${mergedList.size} orders merged from AppSync")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AwsOrder", "Order sync parse error", e)
                } finally {
                    cont.resume(Unit)
                }
            },
            { error ->
                Log.w("AwsOrder", "Order sync query failed", error)
                cont.resume(Unit)
            }
        )
    }

    override fun getAllOrders(): Flow<List<Order>> = ordersState

    override fun getOrdersByUserId(userId: String): Flow<List<Order>> = ordersState.map { list ->
        list.filter { it.userId == userId }
    }

    override suspend fun saveOrder(order: Order) {
        val currentList = ordersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == order.id }
        val idToUse = order.id.ifEmpty { UUID.randomUUID().toString() }
        val orderWithId = order.copy(id = idToUse)
        if (index != -1) {
            currentList[index] = orderWithId
        } else {
            currentList.add(orderWithId)
        }
        ordersState.value = currentList
        persister.saveList("aws_orders.json", currentList)

        // Asynchronously sync to AWS AppSync Order Table
        try {
            val deliveryAddressCombined = "${orderWithId.addressHouseNo} ::: ${orderWithId.addressLandmark} ::: ${orderWithId.customerPhone} ::: ${orderWithId.distanceKm}"
            val itemsInputList = orderWithId.items.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "quantity" to item.quantity,
                    "price" to item.priceAtPurchase.toFloat(),
                    "variantSize" to item.selectedSize
                )
            }
            val inputMap = mapOf(
                "id" to orderWithId.id,
                "customerId" to orderWithId.userId,
                "customerName" to orderWithId.customerName,
                "deliveryAddress" to deliveryAddressCombined,
                "deliveryFee" to orderWithId.deliveryFee.toFloat(),
                "latitude" to orderWithId.latitude.toFloat(),
                "longitude" to orderWithId.longitude.toFloat(),
                "status" to orderWithId.status.name,
                "subtotal" to orderWithId.subtotal.toFloat(),
                "total" to orderWithId.totalAmount.toFloat(),
                "items" to itemsInputList
            )

            val mutation = """
                mutation CreateOrder(${"$"}input: CreateOrderInput!) {
                    createOrder(input: ${"$"}input) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf("input" to inputMap),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> 
                    Log.i("AwsOrder", "Order synced to AWS successfully: ${response.data}")
                    syncScope.launch { fetchOrdersFromCloud() }
                },
                { error -> Log.e("AwsOrder", "AWS Order sync failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsOrder", "AWS Order sync failed", e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        val currentList = ordersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        val status = try { OrderStatus.valueOf(newStatus) } catch (_: Exception) { OrderStatus.PENDING }
        if (index != -1) {
            currentList[index] = currentList[index].copy(status = status)
            ordersState.value = currentList
            persister.saveList("aws_orders.json", currentList)
        }

        // Asynchronously sync status update to AWS AppSync Order Table
        try {
            val mutation = """
                mutation UpdateOrder(${"$"}input: UpdateOrderInput!) {
                    updateOrder(input: ${"$"}input) {
                        id
                        status
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf("input" to mapOf("id" to orderId, "status" to newStatus)),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> 
                    Log.i("AwsOrder", "Order status synced to AWS successfully: ${response.data}")
                    syncScope.launch { fetchOrdersFromCloud() }
                },
                { error -> Log.e("AwsOrder", "AWS Order status sync failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsOrder", "AWS Order status sync failed", e)
        }
    }
}

class AwsUserRepositoryImpl(private val context: Context) : UserRepository {
    private val persister = JsonPersister(context)
    private val usersState = MutableStateFlow(persister.loadList("aws_users.json", User::class.java))

    override fun getUserById(userId: String): Flow<User?> = usersState.map { list ->
        list.find { it.id == userId }
    }

    override suspend fun getUserByEmailAndRole(email: String, role: String): User? {
        return usersState.value.find { it.email == email && it.role == role }
    }

    override suspend fun saveUser(user: User) {
        val currentList = usersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == user.id }
        val idToUse = user.id.ifEmpty { UUID.randomUUID().toString() }
        val userWithId = user.copy(id = idToUse)
        if (index != -1) {
            currentList[index] = userWithId
        } else {
            currentList.add(userWithId)
        }
        usersState.value = currentList
        persister.saveList("aws_users.json", currentList)

        // Asynchronously sync to AWS AppSync User Table
        try {
            val mutation = """
                mutation CreateUser(${"$"}id: ID!, ${"$"}phone: String!, ${"$"}name: String!, ${"$"}role: String!, ${"$"}email: String!) {
                    createUser(input: {id: ${"$"}id, phone: ${"$"}phone, name: ${"$"}name, role: ${"$"}role, email: ${"$"}email}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf(
                    "id" to userWithId.id,
                    "phone" to userWithId.phone,
                    "name" to userWithId.name,
                    "role" to userWithId.role,
                    "email" to userWithId.email
                ),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> Log.i("AwsUser", "User synced to AWS successfully: ${response.data}") },
                { error -> Log.e("AwsUser", "AWS User sync failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsUser", "AWS User sync failed", e)
        }
    }

    override suspend fun deleteUser(userId: String) {
        val currentList = usersState.value.toMutableList()
        currentList.removeAll { it.id == userId }
        usersState.value = currentList
        persister.saveList("aws_users.json", currentList)

        try {
            val mutation = """
                mutation DeleteUser(${"$"}id: ID!) {
                    deleteUser(input: {id: ${"$"}id}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf("id" to userId),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> Log.i("AwsUser", "User deleted on AWS successfully: ${response.data}") },
                { error -> Log.e("AwsUser", "AWS User delete failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsUser", "AWS User delete failed", e)
        }
    }

    override fun getAllUsers(): Flow<List<User>> = usersState
}

class AwsAddressRepositoryImpl(private val context: Context) : AddressRepository {
    private val persister = JsonPersister(context)
    private val addressesState = MutableStateFlow(persister.loadList("aws_addresses.json", Address::class.java))

    override fun getAddressesByUserId(userId: String): Flow<List<Address>> = addressesState.map { list ->
        list.filter { it.userId == userId }
    }

    override suspend fun saveAddress(address: Address) {
        val currentList = addressesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == address.id }
        val idToUse = address.id.ifEmpty { UUID.randomUUID().toString() }
        val addressWithId = address.copy(id = idToUse)
        if (index != -1) {
            currentList[index] = addressWithId
        } else {
            currentList.add(addressWithId)
        }
        addressesState.value = currentList
        persister.saveList("aws_addresses.json", currentList)

        syncAddressToAws(addressWithId)
    }

    override suspend fun saveAndSelectAddress(address: Address) {
        val currentList = addressesState.value.toMutableList()
        // Unselect others for this user
        currentList.forEachIndexed { idx, item ->
            if (item.userId == address.userId) {
                currentList[idx] = item.copy(isSelected = false)
            }
        }
        // Save new
        val index = currentList.indexOfFirst { it.id == address.id }
        val idToUse = address.id.ifEmpty { UUID.randomUUID().toString() }
        val addressWithId = address.copy(id = idToUse, isSelected = true)
        if (index != -1) {
            currentList[index] = addressWithId
        } else {
            currentList.add(addressWithId)
        }
        addressesState.value = currentList
        persister.saveList("aws_addresses.json", currentList)

        syncAddressToAws(addressWithId)
    }

    private fun syncAddressToAws(address: Address) {
        // Asynchronously sync to AWS AppSync Address Table
        try {
            val mutation = """
                mutation CreateAddress(${"$"}id: ID!, ${"$"}userId: String!, ${"$"}houseNo: String!, ${"$"}landmark: String!, ${"$"}distanceKm: Float!, ${"$"}latitude: Float!, ${"$"}longitude: Float!, ${"$"}isSelected: Boolean!) {
                    createAddress(input: {id: ${"$"}id, userId: ${"$"}userId, houseNo: ${"$"}houseNo, landmark: ${"$"}landmark, distanceKm: ${"$"}distanceKm, latitude: ${"$"}latitude, longitude: ${"$"}longitude, isSelected: ${"$"}isSelected}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf(
                    "id" to address.id,
                    "userId" to address.userId,
                    "houseNo" to address.houseNo,
                    "landmark" to address.landmark,
                    "distanceKm" to address.distanceKm.toFloat(),
                    "latitude" to address.latitude.toFloat(),
                    "longitude" to address.longitude.toFloat(),
                    "isSelected" to address.isSelected
                ),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> Log.i("AwsAddress", "Address synced to AWS successfully: ${response.data}") },
                { error -> Log.e("AwsAddress", "AWS Address sync failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsAddress", "AWS Address sync failed", e)
        }
    }

    override suspend fun selectAddress(userId: String, addressId: String) {
        val currentList = addressesState.value.toMutableList()
        currentList.forEachIndexed { idx, item ->
            if (item.userId == userId) {
                currentList[idx] = item.copy(isSelected = item.id == addressId)
            }
        }
        addressesState.value = currentList
        persister.saveList("aws_addresses.json", currentList)

        // Sync changes for selected status
        val selected = currentList.find { it.id == addressId }
        if (selected != null) {
            syncAddressToAws(selected)
        }
    }

    override suspend fun deleteAddress(addressId: String) {
        val currentList = addressesState.value.toMutableList()
        currentList.removeAll { it.id == addressId }
        addressesState.value = currentList
        persister.saveList("aws_addresses.json", currentList)

        try {
            val mutation = """
                mutation DeleteAddress(${"$"}id: ID!) {
                    deleteAddress(input: {id: ${"$"}id}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf("id" to addressId),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> Log.i("AwsAddress", "Address deleted on AWS successfully: ${response.data}") },
                { error -> Log.e("AwsAddress", "AWS Address delete failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsAddress", "AWS Address delete failed", e)
        }
    }
}

object AwsStorageUploader {
    suspend fun uploadProductImage(file: File): String {
        return suspendCancellableCoroutine { cont ->
            Amplify.Storage.uploadFile(
                "products/${file.name}",
                file,
                { result -> cont.resume("https://ricemart-assets.s3.amazonaws.com/public/products/${file.name}") },
                { error -> cont.resume("https://ricemart-assets.s3.amazonaws.com/public/products/${file.name}") }
            )
        }
    }
}
