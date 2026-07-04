package com.example.ui.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.db.RiceMartDatabase
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

    // Role-based Passwords
    private const val ADMIN_PASSWORD = "Ram@123"

    // 3. Observed Lists connected to Room Database
    var productsList by mutableStateOf(emptyList<Product>())
    var addressesList by mutableStateOf(emptyList<Address>())
    var ordersList by mutableStateOf(emptyList<Order>())

    // 4. Cart State (Local customer cart)
    // Map of "product_id#variant_id" to Quantity
    var cartItems by mutableStateOf(mapOf<String, Int>())

    // Initial default seed values — Uncle's 4 real rice brands
    // Admin sets prices and uploads bag images via Inventory screen
    private val initialProducts = listOf(
        Product(
            id = "p_akshaya",
            categoryId = "c_rice",
            nameEn = "Akshaya Rice",
            nameTe = "అక్షయ బియ్యం",
            brand = "Akshaya",
            descriptionEn = "Fresh quality rice delivered directly from the mill. Ideal for everyday home cooking.",
            descriptionTe = "మిల్లు నుండి నేరుగా అందించే తాజా నాణ్యమైన బియ్యం. రోజువారీ వంట కోసం అనువైనది.",
            shortDescriptionEn = "Quality everyday rice from Akshaya brand.",
            shortDescriptionTe = "అక్షయ బ్రాండ్ నాణ్యమైన బియ్యం.",
            imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/akshaya_rice_bag"),
            variants = listOf(
                ProductVariant(id = "ak_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 25, sku = "AK-5KG"),
                ProductVariant(id = "ak_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 5, sku = "AK-10KG"),
                ProductVariant(id = "ak_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 0, sku = "AK-26KG")
            )
        ),
        Product(
            id = "p_sameera",
            categoryId = "c_rice",
            nameEn = "Sameera Rice",
            nameTe = "సమీరా బియ్యం",
            brand = "Sameera",
            descriptionEn = "Trusted Sameera brand rice — soft texture, consistent quality for the whole family.",
            descriptionTe = "నమ్మదగిన సమీరా బ్రాండ్ బియ్యం — మెత్తటి రుచి, మొత్తం కుటుంబానికి నాణ్యత.",
            shortDescriptionEn = "Soft and consistent Sameera rice.",
            shortDescriptionTe = "సమీరా మెత్తటి నాణ్యమైన బియ్యం.",
            imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/sameera_rice_bag"),
            variants = listOf(
                ProductVariant(id = "sm_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 0, sku = "SM-5KG"),
                ProductVariant(id = "sm_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 15, sku = "SM-10KG"),
                ProductVariant(id = "sm_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 3, sku = "SM-26KG")
            )
        ),
        Product(
            id = "p_bell",
            categoryId = "c_rice",
            nameEn = "Bell Brand Rice",
            nameTe = "బెల్ బ్రాండ్ బియ్యం",
            brand = "Bell Brand",
            descriptionEn = "Bell Brand — popular choice for clean, fluffy grains. Great for rice dishes.",
            descriptionTe = "బెల్ బ్రాండ్ — శుభ్రమైన, మృదువైన గింజల కోసం ప్రసిద్ధ ఎంపిక.",
            shortDescriptionEn = "Fluffy clean grains from Bell Brand.",
            shortDescriptionTe = "బెల్ బ్రాండ్ మెత్తటి శుభ్రమైన బియ్యం.",
            imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/bell_brand_rice_bag"),
            variants = listOf(
                ProductVariant(id = "bb_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 8, sku = "BB-5KG"),
                ProductVariant(id = "bb_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 0, sku = "BB-10KG"),
                ProductVariant(id = "bb_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 50, sku = "BB-26KG")
            )
        ),
        Product(
            id = "p_lalitha",
            categoryId = "c_rice",
            nameEn = "Lalitha Brand Rice",
            nameTe = "లలిత బ్రాండ్ బియ్యం",
            brand = "Lalitha Brand",
            descriptionEn = "Lalitha Brand rice — fine quality, light on stomach, perfect for everyday use.",
            descriptionTe = "లలిత బ్రాండ్ బియ్యం — మంచి నాణ్యత, జీర్ణానికి తేలికగా ఉండే రోజువారీ బియ్యం.",
            shortDescriptionEn = "Light and quality Lalitha Brand rice.",
            shortDescriptionTe = "లలిత బ్రాండ్ తేలికైన నాణ్యమైన బియ్యం.",
            imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/lalitha_brand_rice_bag"),
            variants = listOf(
                ProductVariant(id = "lb_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 30, sku = "LB-5KG"),
                ProductVariant(id = "lb_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 2, sku = "LB-10KG"),
                ProductVariant(id = "lb_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 0, sku = "LB-26KG")
            )
        )
    )

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
            return when {
                sub == 0.0 -> 0.0
                sub >= 1000.0 -> 0.0
                else -> 40.0
            }
        }

    val cartTotal: Double
        get() = cartSubtotal + cartDeliveryFee

    // Auth Methods
    // NOTE: Firebase Task callbacks (addOnSuccessListener/addOnFailureListener) always run on
    // the MAIN thread. Do NOT wrap them in ioScope.launch{} — the outer coroutine finishes
    // immediately (before the async Task completes), making inner `launch` calls no-ops.
    fun customerLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            authError = "Email and Password cannot be empty"
            return
        }
        authError = null
        isNetworkLoading = true
        // Direct Firebase Auth call — callbacks fire on main thread
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), pass)
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
                                authError = "Email is not registered as a customer. Please register first."
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
                        "Email is not registered. Please register first."
                    e is FirebaseAuthInvalidCredentialsException ||
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
                    e.message?.contains("credential is incorrect", ignoreCase = true) == true ->
                        "Password incorrect."
                    e.message?.contains("badly formatted", ignoreCase = true) == true ->
                        "Please enter a valid email address."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "No internet connection. Please check your network."
                    else -> "Login failed: ${e.localizedMessage}"
                }
            }
    }

    fun customerRegister(name: String, phone: String, email: String, pass: String) {
        if (name.isBlank()) {
            authError = "Please enter your full name"
            return
        }
        if (phone.isBlank() || phone.length < 10) {
            authError = "Please enter a valid 10-digit phone number"
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            authError = "Please enter a valid email address"
            return
        }
        if (pass.length < 6) {
            authError = "Password must be at least 6 characters"
            return
        }
        authError = null
        isNetworkLoading = true
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), pass)
                .addOnSuccessListener { result ->
                    val firebaseUser = result?.user
                    if (firebaseUser == null) {
                        authError = "Registration failed. Please try again."
                        isNetworkLoading = false
                        return@addOnSuccessListener
                    }
                    val formattedPhone = if (phone.startsWith("+91")) phone else "+91$phone"
                    val newUser = User(
                        id = firebaseUser.uid,
                        phone = formattedPhone,
                        name = name.trim(),
                        role = "CUSTOMER",
                        email = email.trim(),
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
                        e.message?.contains("already in use", ignoreCase = true) == true ->
                            "An account with this email already exists. Please login instead."
                        e.message?.contains("badly formatted", ignoreCase = true) == true ->
                            "Please enter a valid email address."
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

    fun addNewAddress(house: String, landmark: String, distance: Double) {
        val newAddr = Address(
            id = "addr_${System.currentTimeMillis()}",
            userId = currentUser?.id ?: "unknown",
            houseNo = house,
            landmark = landmark,
            distanceKm = distance,
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
        
        // Validate stock levels before placing the order
        var stockError: String? = null
        cartItems.forEach { (key, qty) ->
            val parts = key.split("#")
            if (parts.size == 2) {
                val prodId = parts[0]
                val variantId = parts[1]
                val prod = productsList.find { it.id == prodId }
                val variant = prod?.variants?.find { it.id == variantId }
                if (prod != null && variant != null) {
                    if (qty > variant.stockQuantity) {
                        val brandName = prod.nameEn
                        stockError = "Insufficient stock: Only ${variant.stockQuantity} bags of $brandName (${variant.weight} ${variant.unit}) are available."
                        return@forEach
                    }
                }
            }
        }

        if (stockError != null) {
            withContext(Dispatchers.Main) {
                isNetworkLoading = false
            }
            return@withContext stockError
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

        if (currentItems.isEmpty()) return@withContext "Cart is empty"

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

        // Build a fast lookup: "productId#variantId" -> orderedQty from the cart snapshot
        val cartSnapshot = cartItems.toMap() // safe copy before clearCart
        
        // Deduct stocks only for modified products
        val updatedProducts = mutableListOf<Product>()
        productsList.forEach { prod ->
            var modified = false
            val updatedVariants = prod.variants.map { variant ->
                val cartKey = "${prod.id}#${variant.id}"
                val orderedQty = cartSnapshot[cartKey] ?: 0
                if (orderedQty > 0) {
                    modified = true
                    val newStock = (variant.stockQuantity - orderedQty).coerceAtLeast(0)
                    variant.copy(stockQuantity = newStock)
                } else {
                    variant
                }
            }
            if (modified) {
                updatedProducts.add(prod.copy(variants = updatedVariants, lastUpdated = System.currentTimeMillis()))
            }
        }

        try {
            updatedProducts.forEach { productRepository.saveProduct(it) }
            orderRepository.saveOrder(newOrder)
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
