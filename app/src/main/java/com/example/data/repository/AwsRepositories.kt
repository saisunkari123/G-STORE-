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
        Category(id = "c_rice", nameEn = "Rice Bags", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/rice_bags_preview"),
        Category(id = "c_dal", nameEn = "Dals & Pulses", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/dals_pulses_preview"),
        Category(id = "c_oil", nameEn = "Cooking Oils", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/cooking_oils_preview"),
        Category(id = "c_dairy", nameEn = "Dairy Essentials", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/dairy_essentials_preview"),
        Category(id = "c_spices", nameEn = "Spices & Masalas", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/spices_masalas_preview")
    )
    private val categoriesState = MutableStateFlow(persister.loadList("aws_categories.json", Category::class.java).ifEmpty { defaultCategories })
    private val giftConfigsState = MutableStateFlow(persister.loadList("aws_gifts.json", GiftItemConfig::class.java))
    private val productsState = MutableStateFlow(persister.loadList("aws_products.json", Product::class.java).ifEmpty { emptyList() })
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Start background 5-second cloud sync so all devices always see fresh product list
        startPeriodicCloudSync()
    }

    /** Polls AWS AppSync every 5 seconds and updates the local state if data changed. */
    private fun startPeriodicCloudSync() {
        syncScope.launch {
            while (true) {
                delay(5_000L)
                try {
                    fetchProductsFromCloud()
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
     * Admin-created products on other devices will appear within 5s automatically.
     */
    private suspend fun fetchProductsFromCloud(): Unit = suspendCancellableCoroutine { cont ->
        val query = """
            query ListProducts {
                listProducts(limit: 1000) {
                    items {
                        id
                        categoryId
                        nameEn
                        nameTe
                        brand
                        descriptionEn
                        descriptionTe
                        shortDescriptionEn
                        shortDescriptionTe
                        variantsJson
                        enabled
                        available
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
                                    val variantsJsonStr = raw["variantsJson"] as? String ?: "[]"
                                    val variantType = object : com.google.gson.reflect.TypeToken<List<ProductVariant>>() {}.type
                                    val variants: List<ProductVariant> = gson.fromJson(variantsJsonStr, variantType) ?: emptyList()
                                    Product(
                                        id = raw["id"] as? String ?: "",
                                        categoryId = raw["categoryId"] as? String ?: "",
                                        nameEn = raw["nameEn"] as? String ?: "",
                                        nameTe = raw["nameTe"] as? String ?: "",
                                        brand = raw["brand"] as? String ?: "",
                                        descriptionEn = raw["descriptionEn"] as? String ?: "",
                                        descriptionTe = raw["descriptionTe"] as? String ?: "",
                                        shortDescriptionEn = raw["shortDescriptionEn"] as? String ?: "",
                                        shortDescriptionTe = raw["shortDescriptionTe"] as? String ?: "",
                                        variants = variants,
                                        isEnabled = raw["enabled"] as? Boolean ?: true,
                                        isAvailable = raw["available"] as? Boolean ?: true
                                    )
                                } catch (e: Exception) { null }
                             }
                             
                             // Look for the categories configuration record in the raw items list
                             val sysCatProd = cloudProducts.find { it.id == "sys_categories" }
                             if (sysCatProd != null) {
                                 try {
                                     val type = object : TypeToken<List<Category>>() {}.type
                                     val categories: List<Category> = gson.fromJson(sysCatProd.nameEn, type) ?: emptyList()
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
                                     // Local categories exist but cloud doesn't, so sync them up
                                     syncScope.launch { saveCategories(categoriesState.value) }
                                 }
                             }
                             
                             // Look for sys_gifts configuration record
                             val sysGiftsProd = cloudProducts.find { it.id == "sys_gifts" }
                             if (sysGiftsProd != null) {
                                 try {
                                     val type = object : TypeToken<List<GiftItemConfig>>() {}.type
                                     val gifts: List<GiftItemConfig> = gson.fromJson(sysGiftsProd.nameEn, type) ?: emptyList()
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
                             
                             // Filter sys_categories and sys_gifts out of customer-facing products list
                             val actualProducts = cloudProducts.filter { it.id != "sys_categories" && it.id != "sys_gifts" }
                             productsState.value = actualProducts
                             persister.saveList("aws_products.json", actualProducts)
                             Log.i("AwsProduct", "Cloud sync: ${actualProducts.size} products loaded from AppSync")
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
            nameEn = gson.toJson(categories),
            brand = "System",
            descriptionEn = "System Metadata",
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

    override fun getGiftConfigs(): Flow<List<GiftItemConfig>> = giftConfigsState

    override suspend fun saveGiftConfigs(configs: List<GiftItemConfig>) {
        giftConfigsState.value = configs
        persister.saveList("aws_gifts.json", configs)

        val sysProd = Product(
            id = "sys_gifts",
            categoryId = "metadata",
            nameEn = gson.toJson(configs),
            brand = "System",
            descriptionEn = "System Metadata for Gifts",
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

        if (prodWithId.id != "sys_categories" && prodWithId.id != "sys_gifts") {
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
            val gson = Gson()
            val variantsJson = gson.toJson(prodWithId.variants)
            val mutation = """
                mutation CreateProduct(${"$"}id: ID!, ${"$"}categoryId: String!, ${"$"}nameEn: String!, ${"$"}nameTe: String!, ${"$"}brand: String!, ${"$"}descriptionEn: String!, ${"$"}descriptionTe: String!, ${"$"}shortDescriptionEn: String!, ${"$"}shortDescriptionTe: String!, ${"$"}variantsJson: String!) {
                    createProduct(input: {id: ${"$"}id, categoryId: ${"$"}categoryId, nameEn: ${"$"}nameEn, nameTe: ${"$"}nameTe, brand: ${"$"}brand, descriptionEn: ${"$"}descriptionEn, descriptionTe: ${"$"}descriptionTe, shortDescriptionEn: ${"$"}shortDescriptionEn, shortDescriptionTe: ${"$"}shortDescriptionTe, variantsJson: ${"$"}variantsJson}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf(
                    "id" to prodWithId.id,
                    "categoryId" to prodWithId.categoryId,
                    "nameEn" to prodWithId.nameEn,
                    "nameTe" to prodWithId.nameTe,
                    "brand" to prodWithId.brand,
                    "descriptionEn" to prodWithId.descriptionEn,
                    "descriptionTe" to prodWithId.descriptionTe,
                    "shortDescriptionEn" to prodWithId.shortDescriptionEn,
                    "shortDescriptionTe" to prodWithId.shortDescriptionTe,
                    "variantsJson" to variantsJson
                ),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> 
                    Log.i("AwsProduct", "Product synced to AWS successfully: ${response.data}")
                    syncScope.launch { forceRefreshFromCloud() }
                },
                { error -> Log.e("AwsProduct", "AWS Product sync failed", error) }
            )
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
                mutation DeleteProduct(${"$"}id: ID!) {
                    deleteProduct(input: {id: ${"$"}id}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf("id" to productId),
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
    private val ordersState = MutableStateFlow(persister.loadList("aws_orders.json", Order::class.java))

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
            val gson = Gson()
            val itemsJson = gson.toJson(orderWithId.items)
            val mutation = """
                mutation CreateOrder(${"$"}id: ID!, ${"$"}userId: String!, ${"$"}customerName: String!, ${"$"}customerPhone: String!, ${"$"}addressHouseNo: String!, ${"$"}addressLandmark: String!, ${"$"}distanceKm: Float!, ${"$"}subtotal: Float!, ${"$"}deliveryFee: Float!, ${"$"}totalAmount: Float!, ${"$"}status: String!, ${"$"}createdAt: String!, ${"$"}itemsJson: String!) {
                    createOrder(input: {id: ${"$"}id, userId: ${"$"}userId, customerName: ${"$"}customerName, customerPhone: ${"$"}customerPhone, addressHouseNo: ${"$"}addressHouseNo, addressLandmark: ${"$"}addressLandmark, distanceKm: ${"$"}distanceKm, subtotal: ${"$"}subtotal, deliveryFee: ${"$"}deliveryFee, totalAmount: ${"$"}totalAmount, status: ${"$"}status, createdAt: ${"$"}createdAt, itemsJson: ${"$"}itemsJson}) {
                        id
                    }
                }
            """.trimIndent()
            val request = SimpleGraphQLRequest<String>(
                mutation,
                mapOf(
                    "id" to orderWithId.id,
                    "userId" to orderWithId.userId,
                    "customerName" to orderWithId.customerName,
                    "customerPhone" to orderWithId.customerPhone,
                    "addressHouseNo" to orderWithId.addressHouseNo,
                    "addressLandmark" to orderWithId.addressLandmark,
                    "distanceKm" to orderWithId.distanceKm.toFloat(),
                    "subtotal" to orderWithId.subtotal.toFloat(),
                    "deliveryFee" to orderWithId.deliveryFee.toFloat(),
                    "totalAmount" to orderWithId.totalAmount.toFloat(),
                    "status" to orderWithId.status.name,
                    "createdAt" to orderWithId.createdAt.toString(),
                    "itemsJson" to itemsJson
                ),
                String::class.java,
                GsonVariablesSerializer()
            )
            Amplify.API.mutate(request,
                { response -> Log.i("AwsOrder", "Order synced to AWS successfully: ${response.data}") },
                { error -> Log.e("AwsOrder", "AWS Order sync failed", error) }
            )
        } catch (e: Exception) {
            Log.e("AwsOrder", "AWS Order sync failed", e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        val currentList = ordersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val status = try { OrderStatus.valueOf(newStatus) } catch (_: Exception) { OrderStatus.PENDING }
            currentList[index] = currentList[index].copy(status = status)
            ordersState.value = currentList
            persister.saveList("aws_orders.json", currentList)

            // Asynchronously sync status update to AWS AppSync Order Table
            try {
                val mutation = """
                    mutation UpdateOrder(${"$"}id: ID!, ${"$"}status: String!) {
                        updateOrder(input: {id: ${"$"}id, status: ${"$"}status}) {
                            id
                        }
                    }
                """.trimIndent()
                val request = SimpleGraphQLRequest<String>(
                    mutation,
                    mapOf("id" to orderId, "status" to newStatus),
                    String::class.java,
                    GsonVariablesSerializer()
                )
                Amplify.API.mutate(request,
                    { response -> Log.i("AwsOrder", "Order status synced to AWS successfully: ${response.data}") },
                    { error -> Log.e("AwsOrder", "AWS Order status sync failed", error) }
                )
            } catch (e: Exception) {
                Log.e("AwsOrder", "AWS Order status sync failed", e)
            }
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
