package com.example.ui.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.repository.*
import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

object AppState {
    val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, t -> t.printStackTrace() })

    // Repository references
    lateinit var productRepository: ProductRepository
    lateinit var addressRepository: AddressRepository
    lateinit var orderRepository: OrderRepository
    lateinit var userRepository: UserRepository

    // 1. App State
    var forceStoreOpen by mutableStateOf(true)
    // 2. Auth State
    private var _currentUser = mutableStateOf<User?>(null)
    var currentUser: User?
        get() = _currentUser.value
        set(value) {
            _currentUser.value = value
            if (value != null) {
                // BUG-F2 FIX: Only restart the products listener if it isn't already active.
                // initializeDatabase() starts it at launch; no need to restart on every login/session restore.
                if (productsJob == null || !productsJob!!.isActive) {
                    observeProducts()
                }
                observeAddresses(value.id)
                observeOrders(value.id, value.role)
            } else {
                // BUG-F3 FIX: Also cancel the products listener on logout — prevents Firestore read leak.
                productsJob?.cancel()
                addressesJob?.cancel()
                ordersJob?.cancel()
                productsList = emptyList()
                addressesList = emptyList()
                ordersList = emptyList()
            }
        }
    var showLoginScreen by mutableStateOf(true)
    var isRegistering by mutableStateOf(true) // Default to registration screen
    var authError by mutableStateOf<String?>(null)
    var isNetworkLoading by mutableStateOf(false)
    var lastPlacedOrder by mutableStateOf<Order?>(null)
    var isPlacingOrder by mutableStateOf(false)
    var isDarkMode by mutableStateOf(false)
    var monthlySalesGoal by mutableStateOf(50000.0)

    // Role-based Passwords
    private val ADMIN_PASSWORD = com.example.BuildConfig.ADMIN_PASSWORD

    // Shop Location & Delivery Boundaries (Hyderabad Center)
    const val SHOP_LATITUDE = 17.4065
    const val SHOP_LONGITUDE = 78.4772
    const val MAX_DELIVERY_DISTANCE_KM = 10.0

    // 3. Observed Lists connected to Room Database
    var productsList by mutableStateOf(emptyList<Product>())
    var addressesList by mutableStateOf(emptyList<Address>())
    var ordersList by mutableStateOf(emptyList<Order>())

    // 4. Cart State (Local customer cart)
    // Map of "product_id#variant_id" to Quantity
    var cartItems by mutableStateOf(mapOf<String, Int>())





    // Helper to calculate cart metrics
    val cartSubtotal: Double
        get() {
            var sum = 0.0
            cartItems.forEach { (key, qty) ->
                val parts = key.split("#")
                if (parts.size == 2) {
                    val prodId = parts[0]
                    val variantId = parts[1]
                    val prod = productsList.find { it.id == prodId }
                    val variant = prod?.variants?.find { it.id == variantId }
                    if (variant != null) {
                        sum += variant.currentPrice * qty
                    }
                }
            }
            return sum
        }

    val cartDeliveryFee: Double
        get() {
            val sub = cartSubtotal
            if (sub == 0.0) return 0.0
            val selectedAddr = addressesList.find { it.isSelected } ?: return 40.0
            return when {
                selectedAddr.distanceKm <= 2.0 -> 0.0
                selectedAddr.distanceKm <= MAX_DELIVERY_DISTANCE_KM -> {
                    // Charge $10 per kilometer, rounded up for clean pricing
                    Math.ceil(selectedAddr.distanceKm) * 10.0
                }
                else -> 0.0 // Will be blocked during checkout
            }
        }

    val cartTotal: Double
        get() = cartSubtotal + cartDeliveryFee

    // Auth Methods
    // NOTE: Firebase Task callbacks (addOnSuccessListener/addOnFailureListener) always run on
    // the MAIN thread. Do NOT wrap them in ioScope.launch{} — the outer coroutine finishes
    // immediately (before the async Task completes), making inner `launch` calls no-ops.
    fun customerLogin(phone: String, pass: String) {
        if (phone.isBlank() || pass.isBlank()) {
            authError = "Phone Number and Password cannot be empty"
            return
        }
        authError = null
        isNetworkLoading = true
        
        val digitsOnly = phone.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly
        val dummyEmail = "$cleanPhone@ricemart.app"
        
        // Direct Firebase Auth call — callbacks fire on main thread
        FirebaseAuth.getInstance().signInWithEmailAndPassword(dummyEmail, pass)
            .addOnSuccessListener { result ->
                val firebaseUser = result?.user
                if (firebaseUser == null) {
                    authError = "Login failed. Please try again."
                    isNetworkLoading = false
                    return@addOnSuccessListener
                }
                // Firestore read needs IO coroutine, then switch back to Main for state update
                ioScope.launch {
                    try {
                        val userDoc = userRepository.getUserById(firebaseUser.uid).first()
                        withContext(Dispatchers.Main) {
                            if (userDoc != null && userDoc.role == "CUSTOMER") {
                                currentUser = userDoc
                                activeRole = userDoc.role
                                showLoginScreen = false
                                authError = null
                                isNetworkLoading = false
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                currentUser = null
                                authError = "Phone number is not registered as a customer. Please register first."
                                isNetworkLoading = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            authError = "Could not load profile: ${e.localizedMessage}"
                            isNetworkLoading = false
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                isNetworkLoading = false
                // This runs on main thread — set authError directly
                authError = when {
                    e is FirebaseAuthInvalidUserException ->
                        "Phone number is not registered. Please register first."
                    e is FirebaseAuthInvalidCredentialsException ||
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
                    e.message?.contains("credential is incorrect", ignoreCase = true) == true ->
                        "Password incorrect."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "No internet connection. Please check your network."
                    else -> "Login failed: ${e.localizedMessage}"
                }
            }
    }

    fun customerRegister(name: String, phone: String, pass: String) {
        if (name.isBlank()) {
            authError = "Please enter your full name"
            return
        }
        val digitsOnly = phone.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly
        if (cleanPhone.length < 10) {
            authError = "Please enter a valid 10-digit phone number"
            return
        }
        val hasLetter = pass.any { it.isLetter() }
        val hasDigit = pass.any { it.isDigit() }
        val hasSymbol = pass.any { !it.isLetterOrDigit() }
        if (pass.length < 8 || !hasLetter || !hasDigit || !hasSymbol) {
            authError = "Password must be at least 8 characters with letters, numbers, and symbols"
            return
        }
        authError = null
        isNetworkLoading = true
        val dummyEmail = "$cleanPhone@ricemart.app"
        val formattedPhone = "+91$cleanPhone"
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(dummyEmail, pass)
                .addOnSuccessListener { result ->
                    val firebaseUser = result?.user
                    if (firebaseUser == null) {
                        authError = "Registration failed. Please try again."
                        isNetworkLoading = false
                        return@addOnSuccessListener
                    }
                    val newUser = User(
                        id = firebaseUser.uid,
                        phone = formattedPhone,
                        name = name.trim(),
                        role = "CUSTOMER",
                        email = dummyEmail,
                        pinOrPassword = "" // Don't store password in Firestore
                    )
                    // Save user profile to Firestore
                    ioScope.launch {
                        try {
                            userRepository.saveUser(newUser)
                            launch(Dispatchers.Main) {
                                currentUser = newUser
                                activeRole = "CUSTOMER"
                                showLoginScreen = false
                                isRegistering = false
                                authError = null
                                isNetworkLoading = false
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            launch(Dispatchers.Main) {
                                // Auth succeeded but Firestore save failed
                                // Still let the user in with the data we have
                                currentUser = newUser
                                activeRole = "CUSTOMER"
                                showLoginScreen = false
                                isRegistering = false
                                authError = null
                                isNetworkLoading = false
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    val msg = when {
                        e.message?.contains("already in use", ignoreCase = true) == true ||
                        e.message?.contains("already exists", ignoreCase = true) == true ->
                            "An account with this phone number already exists. Please login instead."
                        e.message?.contains("weak password", ignoreCase = true) == true ->
                            "Password is too weak. Use at least 6 characters."
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "No internet connection. Please check your network."
                        else -> "Registration failed: ${e.localizedMessage}"
                    }
                    authError = msg
                    isNetworkLoading = false
                }
        } catch (e: Exception) {
            e.printStackTrace()
            authError = "Unexpected error: ${e.localizedMessage}"
            isNetworkLoading = false
        }
    }

    fun loginAsAdmin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            authError = "Email and Password cannot be empty"
            return
        }
        authError = null
        isNetworkLoading = true
        val emailTrim = email.trim()
        // Direct Firebase Auth call — callbacks fire on main thread
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailTrim, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result?.user ?: run {
                    authError = "Login failed. Please try again."
                    isNetworkLoading = false
                    return@addOnSuccessListener
                }
                
                // If it is the default admin, set state immediately to avoid any Firestore read/write blocks during login transition
                if (emailTrim == "admin@gstore.com") {
                    val adminUser = User(
                        id = firebaseUser.uid,
                        phone = "+91 00000 00001",
                        name = "G-STORE Admin",
                        role = "ADMIN",
                        email = emailTrim,
                        pinOrPassword = ADMIN_PASSWORD
                    )
                    currentUser = adminUser
                    activeRole = "ADMIN"
                    showLoginScreen = false
                    authError = null
                    isNetworkLoading = false
                    
                    // Always seed/refresh the 4 brand products.
                    // Delete legacy products (old IDs) if they exist, then save new ones.
                    ioScope.launch {
                        try {
                            userRepository.saveUser(adminUser)
                            // Remove old generic seed products if still in Firestore
                            val legacyIds = listOf("p_sonamasoori", "p_basmati")
                            legacyIds.map { id ->
                                async { try { productRepository.deleteProduct(id) } catch (_: Exception) {} }
                            }.awaitAll()
                            // Always save the 4 real brand products (upsert — safe to run every login)
                            productRepository.saveProducts(initialProducts)
                        } catch (err: Exception) {
                            err.printStackTrace()
                        }
                    }
                    return@addOnSuccessListener
                }

                ioScope.launch {
                    try {
                        val userDoc = userRepository.getUserById(firebaseUser.uid).first()
                        if (userDoc != null && userDoc.role == "ADMIN") {
                            withContext(Dispatchers.Main) {
                                currentUser = userDoc
                                activeRole = userDoc.role
                                showLoginScreen = false
                                authError = null
                                isNetworkLoading = false
                            }
                        } else {
                            // Firebase user exists but no ADMIN profile in Firestore — create it.
                            // BUG-F1 FIX: saveUser() is a suspend Firestore call — must stay on IO,
                            // NOT inside withContext(Dispatchers.Main) to avoid blocking the UI thread.
                            val adminUser = User(
                                id = firebaseUser.uid,
                                phone = "+91 00000 00001",
                                name = "G-STORE Admin",
                                role = "ADMIN",
                                email = emailTrim,
                                pinOrPassword = ADMIN_PASSWORD
                            )
                            userRepository.saveUser(adminUser) // runs on IO ✓
                            withContext(Dispatchers.Main) {
                                currentUser = adminUser
                                activeRole = "ADMIN"
                                showLoginScreen = false
                                authError = null
                                isNetworkLoading = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            authError = "Login Failed: ${e.localizedMessage}"
                            isNetworkLoading = false
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                if (emailTrim == "admin@gstore.com" && password == ADMIN_PASSWORD) {
                    // Admin not in Firebase Auth yet — auto-register with seeded credentials
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailTrim, password)
                        .addOnSuccessListener { result ->
                            val firebaseUser = result?.user ?: return@addOnSuccessListener
                            val adminUser = User(
                                id = firebaseUser.uid,
                                phone = "+91 00000 00001",
                                name = "G-STORE Admin",
                                role = "ADMIN",
                                email = emailTrim,
                                pinOrPassword = ADMIN_PASSWORD
                            )
                            
                            // Log in immediately
                            currentUser = adminUser
                            activeRole = "ADMIN"
                            showLoginScreen = false
                            authError = null
                            isNetworkLoading = false
                            
                            // Always seed/refresh the 4 brand products (new admin auto-register path)
                            ioScope.launch {
                                try {
                                    userRepository.saveUser(adminUser)
                                    val legacyIds = listOf("p_sonamasoori", "p_basmati")
                                    legacyIds.map { id ->
                                        async { try { productRepository.deleteProduct(id) } catch (_: Exception) {} }
                                    }.awaitAll()
                                    productRepository.saveProducts(initialProducts)
                                } catch (err: Exception) {
                                    err.printStackTrace()
                                }
                            }
                        }
                        .addOnFailureListener { err ->
                            authError = "Login Failed: ${err.localizedMessage}"
                            isNetworkLoading = false
                        }
                } else {
                    // This runs on main thread — set authError directly
                    isNetworkLoading = false
                    authError = when {
                        emailTrim != "admin@gstore.com" -> "Admin account not found"
                        else -> "Invalid Admin Password"
                    }
                }
            }
    }

    fun logout() {
        try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
        currentUser = null
        showLoginScreen = true
        activeRole = "CUSTOMER"
        isRegistering = true
        authError = null
        isNetworkLoading = false
    }

    // Selected active role for the Developer switcher
    var activeRole by mutableStateOf("CUSTOMER") // "CUSTOMER", "ADMIN"

    // Initial setups
    init {
        // No default user, force login
    }

    private var addressesJob: Job? = null
    private var ordersJob: Job? = null
    private var productsJob: Job? = null

    private fun observeProducts() {
        productsJob?.cancel()
        productsJob = ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    productRepository.getAllProducts().collect { list ->
                        withContext(Dispatchers.Main) {
                            productsList = list
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) { isNetworkLoading = false; if (e.message?.contains("network", ignoreCase = true) == true) authError = "No internet connection" }
            }
        }
    }

    private fun observeAddresses(userId: String) {
        addressesJob?.cancel()
        addressesJob = ioScope.launch {
            try {
                if (::addressRepository.isInitialized) {
                    addressRepository.getAddressesByUserId(userId).collect { list ->
                        withContext(Dispatchers.Main) {
                            addressesList = list
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) { isNetworkLoading = false; if (e.message?.contains("network", ignoreCase = true) == true) authError = "No internet connection" }
            }
        }
    }

    private fun observeOrders(userId: String, role: String) {
        ordersJob?.cancel()
        ordersJob = ioScope.launch {
            try {
                if (::orderRepository.isInitialized) {
                    val flow = if (role == "ADMIN") {
                        orderRepository.getAllOrders()
                    } else {
                        orderRepository.getOrdersByUserId(userId)
                    }
                    flow.collect { list ->
                        withContext(Dispatchers.Main) {
                            ordersList = list
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) { isNetworkLoading = false; if (e.message?.contains("network", ignoreCase = true) == true) authError = "No internet connection" }
            }
        }
    }

    private var isDbInitialized = false

    fun initializeDatabase(context: Context) {
        if (isDbInitialized) return
        isDbInitialized = true

        // Now using Firebase Firestore Repositories instead of Local Room DB
        productRepository = com.example.data.repository.FirebaseProductRepositoryImpl()
        addressRepository = com.example.data.repository.FirebaseAddressRepositoryImpl()
        orderRepository = com.example.data.repository.FirebaseOrderRepositoryImpl()
        userRepository = com.example.data.repository.FirebaseUserRepositoryImpl()

        // Start observing products immediately (succeeds if Firestore rules allow public reads)
        observeProducts()

        ioScope.launch {
            // Restore active Firebase Auth user session if exists
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                try {
                    val userDoc = userRepository.getUserById(firebaseUser.uid).first()
                    withContext(Dispatchers.Main) {
                        if (userDoc != null) {
                            currentUser = userDoc  // this calls observeProducts() again after auth
                            activeRole = userDoc.role
                            showLoginScreen = false
                            authError = null
                        } else {
                            FirebaseAuth.getInstance().signOut()
                            currentUser = null
                            showLoginScreen = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) { showLoginScreen = true }
                }
            } else {
                withContext(Dispatchers.Main) {
                    currentUser = null
                    showLoginScreen = true
                }
            }
        }
    }

    // Methods
    fun addToCart(productId: String, variantId: String) {
        val key = "$productId#$variantId"
        val currentQty = cartItems[key] ?: 0
        val prod = productsList.find { it.id == productId }
        val variant = prod?.variants?.find { it.id == variantId }
        val maxStock = variant?.stockQuantity ?: 0
        if (currentQty < maxStock) {
            val newMap = cartItems.toMutableMap()
            newMap[key] = currentQty + 1
            cartItems = newMap
        }
    }

    fun updateCartQty(productId: String, variantId: String, qty: Int) {
        val key = "$productId#$variantId"
        val prod = productsList.find { it.id == productId }
        val variant = prod?.variants?.find { it.id == variantId }
        val maxStock = variant?.stockQuantity ?: 0
        val safeQty = qty.coerceAtMost(maxStock)

        val newMap = cartItems.toMutableMap()
        if (safeQty <= 0) {
            newMap.remove(key)
        } else {
            newMap[key] = safeQty
        }
        cartItems = newMap
    }

    fun clearCart() {
        cartItems = emptyMap()
    }

    fun addNewAddress(house: String, landmark: String, distance: Double, lat: Double = 0.0, lon: Double = 0.0) {
        val newAddr = Address(
            id = "addr_${System.currentTimeMillis()}",
            userId = currentUser?.id ?: "unknown",
            houseNo = house,
            landmark = landmark,
            distanceKm = distance,
            latitude = lat,
            longitude = lon,
            isSelected = true
        )
        ioScope.launch {
            addressRepository.saveAndSelectAddress(newAddr)
        }
    }

    fun selectAddress(addressId: String) {
        val userId = currentUser?.id ?: "cust_1"
        ioScope.launch {
            addressRepository.selectAddress(userId, addressId)
        }
    }

    fun deleteAddress(addressId: String) {
        ioScope.launch {
            try {
                if (::addressRepository.isInitialized) {
                    addressRepository.deleteAddress(addressId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) { isNetworkLoading = false; if (e.message?.contains("network", ignoreCase = true) == true) authError = "No internet connection" }
            }
        }
    }

    fun editAddress(addressId: String, newHouseNo: String, newLandmark: String) {
        val existing = addressesList.find { it.id == addressId } ?: return
        val updated = existing.copy(houseNo = newHouseNo, landmark = newLandmark)
        ioScope.launch {
            try {
                if (::addressRepository.isInitialized) {
                    addressRepository.saveAddress(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) { isNetworkLoading = false; if (e.message?.contains("network", ignoreCase = true) == true) authError = "No internet connection" }
            }
        }
    }

    suspend fun placeOrder(): String? = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            isNetworkLoading = true
        }
        val selectedAddr = addressesList.find { it.isSelected }
        if (selectedAddr == null) {
            withContext(Dispatchers.Main) {
                isNetworkLoading = false
            }
            return@withContext "No address selected"
        }

        if (selectedAddr.distanceKm > MAX_DELIVERY_DISTANCE_KM) {
            withContext(Dispatchers.Main) {
                isNetworkLoading = false
            }
            return@withContext "Sorry, we only deliver within $MAX_DELIVERY_DISTANCE_KM km of the shop."
        }

        val currentItems = mutableListOf<OrderItem>()
        cartItems.forEach { (key, qty) ->
            val parts = key.split("#")
            if (parts.size == 2) {
                val prodId = parts[0]
                val variantId = parts[1]
                val prod = productsList.find { it.id == prodId }
                val variant = prod?.variants?.find { it.id == variantId }
                if (prod != null && variant != null) {
                    currentItems.add(OrderItem(
                        productId = prod.id,
                        productName = prod.nameEn,
                        selectedSize = "${variant.weight} ${variant.unit}",
                        priceAtPurchase = variant.currentPrice,
                        quantity = qty
                    ))
                }
            }
        }

        if (currentItems.isEmpty()) {
            withContext(Dispatchers.Main) {
                isNetworkLoading = false
            }
            return@withContext "Cart is empty"
        }

        withContext(Dispatchers.Main) {
            isPlacingOrder = true
        }

        val newOrder = Order(
            id = "G-${System.currentTimeMillis()}-${(100..999).random()}",
            userId = currentUser?.id ?: "guest",
            customerName = currentUser?.name ?: "Valued Customer",
            customerPhone = currentUser?.phone ?: "+91 98765 43210",
            addressHouseNo = selectedAddr.houseNo,
            addressLandmark = selectedAddr.landmark,
            distanceKm = selectedAddr.distanceKm,
            subtotal = cartSubtotal,
            deliveryFee = cartDeliveryFee,
            totalAmount = cartTotal,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            items = currentItems
        )

        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        try {
            firestore.runTransaction { transaction ->
                val cartSnapshot = cartItems.toMap()
                val uniqueProductIds = cartSnapshot.keys.map { it.split("#")[0] }.distinct()

                // 1. Fetch fresh products from the database inside the transaction
                val dbProducts = uniqueProductIds.associateWith { prodId ->
                    val ref = firestore.collection("products").document(prodId)
                    val snapshot = transaction.get(ref)
                    val prod = snapshot.toObject(Product::class.java)
                        ?: throw Exception("Product not found in store catalog.")
                    prod
                }

                // 2. Validate stock levels and compute updated variants
                val productsToUpdate = mutableListOf<Product>()
                dbProducts.forEach { (prodId, prod) ->
                    var modified = false
                    val updatedVariants = prod.variants.map { variant ->
                        val cartKey = "$prodId#${variant.id}"
                        val orderedQty = cartSnapshot[cartKey] ?: 0
                        if (orderedQty > 0) {
                            if (orderedQty > variant.stockQuantity) {
                                throw Exception("Insufficient stock: Only ${variant.stockQuantity} bags of ${prod.nameEn} (${variant.weight} ${variant.unit}) are available.")
                            }
                            modified = true
                            variant.copy(stockQuantity = variant.stockQuantity - orderedQty)
                        } else {
                            variant
                        }
                    }
                    if (modified) {
                        productsToUpdate.add(prod.copy(variants = updatedVariants, lastUpdated = System.currentTimeMillis()))
                    }
                }

                // 3. Write updated product stock records
                productsToUpdate.forEach { prod ->
                    val ref = firestore.collection("products").document(prod.id)
                    transaction.set(ref, prod)
                }

                // 4. Save the order document
                val orderRef = firestore.collection("orders").document(newOrder.id.ifEmpty { java.util.UUID.randomUUID().toString() })
                val orderToSave = newOrder.copy(id = orderRef.id)
                transaction.set(orderRef, orderToSave)
            }.await()
            withContext(Dispatchers.Main) {
                lastPlacedOrder = newOrder
                clearCart()
                isPlacingOrder = false
                isNetworkLoading = false
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                isPlacingOrder = false
                isNetworkLoading = false
            }
            e.localizedMessage ?: "Unknown error occurred"
        }
    }

    fun adminUpdateProductVariant(productId: String, variant: ProductVariant) {
        ioScope.launch {
            val prod = productsList.find { it.id == productId }
            if (prod != null) {
                val updatedVariants = prod.variants.map {
                    if (it.id == variant.id) variant
                    else it
                }.toMutableList()
                
                if (prod.variants.none { it.id == variant.id }) {
                    updatedVariants.add(variant)
                }

                productRepository.saveProduct(prod.copy(variants = updatedVariants, lastUpdated = System.currentTimeMillis()))
            }
        }
    }

    fun adminDeleteProductVariant(productId: String, variantId: String) {
        ioScope.launch {
            val prod = productsList.find { it.id == productId }
            if (prod != null) {
                val updatedVariants = prod.variants.filter { it.id != variantId }
                productRepository.saveProduct(prod.copy(variants = updatedVariants, lastUpdated = System.currentTimeMillis()))
            }
        }
    }

    fun adminAddProduct(
        nameEn: String, 
        brand: String,
        descEn: String, 
        imageUrls: List<String>, 
        variants: List<ProductVariant>
    ) {
        val newProd = Product(
            id = "p_${System.currentTimeMillis()}",
            categoryId = "c_rice",
            nameEn = nameEn,
            nameTe = "",
            brand = brand,
            descriptionEn = descEn,
            descriptionTe = "",
            shortDescriptionEn = descEn.take(50),
            shortDescriptionTe = "",
            imageUrls = imageUrls,
            variants = variants
        )
        ioScope.launch {
            productRepository.saveProduct(newProd)
        }
    }

    fun adminUpdateProduct(product: Product) {
        ioScope.launch {
            productRepository.saveProduct(product)
        }
    }

    fun adminDeleteProduct(productId: String) {
        ioScope.launch {
            productRepository.deleteProduct(productId)
        }
    }

    // Calculates distance in kilometers between two GPS coordinates using the Haversine formula (100% Free Offline)
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }


    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        isNetworkLoading = true
        ioScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, newStatus.name)
                withContext(Dispatchers.Main) {
                    isNetworkLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isNetworkLoading = false
                }
            }
        }
    }
}
