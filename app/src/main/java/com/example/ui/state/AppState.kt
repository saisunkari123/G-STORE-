package com.example.ui.state

import android.content.Context
import java.util.UUID
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.result.AuthResetPasswordResult
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.AuthUserAttributeKey
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


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
                if (categoriesJob == null || !categoriesJob!!.isActive) {
                    observeCategories()
                }
                if (giftsJob == null || !giftsJob!!.isActive) {
                    observeGifts()
                }
                observeAddresses(value.id)
                observeOrders(value.id, value.role)
            } else {
                // BUG-F3 FIX: Also cancel the products listener on logout — prevents Firestore read leak.
                productsJob?.cancel()
                categoriesJob?.cancel()
                giftsJob?.cancel()
                addressesJob?.cancel()
                ordersJob?.cancel()
                productsList = emptyList()
                categoriesList = emptyList()
                giftConfigsList = emptyList()
                addressesList = emptyList()
                ordersList = emptyList()
            }
        }
    var isInitializingSession by mutableStateOf(true)
    var showLoginScreen by mutableStateOf(true)
    var isRegistering by mutableStateOf(false) // Default to login screen
    var authError by mutableStateOf<String?>(null)
    var isNetworkLoading by mutableStateOf(false)
    var lastPlacedOrder by mutableStateOf<Order?>(null)
    var isPlacingOrder by mutableStateOf(false)
    var isDarkMode by mutableStateOf(false)
    var monthlySalesGoal by mutableStateOf(50000.0)

    // Role-based Passwords
    private val ADMIN_PASSWORD = com.example.BuildConfig.ADMIN_PASSWORD

    // Shop Location & Delivery Boundaries (City Super Market, Rajam, Vizianagaram)
    const val SHOP_LATITUDE = 18.4482
    const val SHOP_LONGITUDE = 83.6616
    const val MAX_DELIVERY_DISTANCE_KM = 10.0

    // 3. Observed Lists connected to Room Database
    var productsList by mutableStateOf(emptyList<Product>())
    var categoriesList by mutableStateOf(emptyList<Category>())
    var giftConfigsList by mutableStateOf(emptyList<GiftItemConfig>())
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
        get() = 0.0

    val cartTotal: Double
        get() = cartSubtotal + cartDeliveryFee

    // Auth Methods
    // NOTE: Firebase Task callbacks (addOnSuccessListener/addOnFailureListener) always run on
    // the MAIN thread. Do NOT wrap them in ioScope.launch{} — the outer coroutine finishes
    // immediately (before the async Task completes), making inner `launch` calls no-ops.
    // Remote DB Lookup Helpers to bypass local cache out-of-sync issues
    private suspend fun getUserByPhoneRemote(phone: String): User? = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        val queryStr = """
            query ListUsersByPhone(${"$"}phone: String!) {
                listUsers(filter: {phone: {eq: ${"$"}phone}}) {
                    items {
                        id
                        email
                        name
                        phone
                        role
                    }
                }
            }
        """.trimIndent()
        val request = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
            queryStr,
            mapOf("phone" to phone),
            String::class.java,
            com.amplifyframework.api.aws.GsonVariablesSerializer()
        )
        Amplify.API.query(request,
            { response ->
                try {
                    val json = response.data
                    if (json != null) {
                        val gson = com.google.gson.Gson()
                        val root = gson.fromJson(json, Map::class.java)
                        val listUsers = root["listUsers"] as? Map<*, *>
                        val items = listUsers?.get("items") as? List<*>
                        if (!items.isNullOrEmpty()) {
                            val itemJson = gson.toJson(items[0])
                            val user = gson.fromJson(itemJson, User::class.java)
                            continuation.resume(user)
                        } else {
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            },
            { error ->
                continuation.resume(null)
            }
        )
    }

    private suspend fun getUserByEmailRemote(email: String): User? = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        val queryStr = """
            query ListUsersByEmail(${"$"}email: String!) {
                listUsers(filter: {email: {eq: ${"$"}email}}) {
                    items {
                        id
                        email
                        name
                        phone
                        role
                    }
                }
            }
        """.trimIndent()
        val request = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
            queryStr,
            mapOf("email" to email),
            String::class.java,
            com.amplifyframework.api.aws.GsonVariablesSerializer()
        )
        Amplify.API.query(request,
            { response ->
                try {
                    val json = response.data
                    if (json != null) {
                        val gson = com.google.gson.Gson()
                        val root = gson.fromJson(json, Map::class.java)
                        val listUsers = root["listUsers"] as? Map<*, *>
                        val items = listUsers?.get("items") as? List<*>
                        if (!items.isNullOrEmpty()) {
                            val itemJson = gson.toJson(items[0])
                            val user = gson.fromJson(itemJson, User::class.java)
                            continuation.resume(user)
                        } else {
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            },
            { error ->
                continuation.resume(null)
            }
        )
    }

    fun restoreSession() {
        ioScope.launch {
            try {
                // Using AWS Amplify Auth to fetch the current session
                val session = suspendCancellableCoroutine<com.amplifyframework.auth.AuthSession> { continuation ->
                    Amplify.Auth.fetchAuthSession(
                        { result -> continuation.resume(result) },
                        { error -> continuation.resumeWith(Result.failure(error)) }
                    )
                }

                if (session.isSignedIn) {
                    // Session exists. Fetch current user from Amplify
                    val authUser = suspendCancellableCoroutine<com.amplifyframework.auth.AuthUser> { continuation ->
                        Amplify.Auth.getCurrentUser(
                            { result -> continuation.resume(result) },
                            { error -> continuation.resumeWith(Result.failure(error)) }
                        )
                    }

                    // We use the email/username as the key or we can query by email/phone.
                    // For our custom remote lookup, check if email/phone match. 
                    // To do this simply, we can get user attributes.
                    val attributes = suspendCancellableCoroutine<List<com.amplifyframework.auth.AuthUserAttribute>> { continuation ->
                        Amplify.Auth.fetchUserAttributes(
                            { result -> continuation.resume(result) },
                            { error -> continuation.resumeWith(Result.failure(error)) }
                        )
                    }

                    val email = attributes.find { it.key == AuthUserAttributeKey.email() }?.value ?: ""
                    val phone = attributes.find { it.key == AuthUserAttributeKey.phoneNumber() }?.value ?: ""

                    // Search for user in remote database first by phone, then email
                    var user = if (phone.isNotBlank()) getUserByPhoneRemote(phone) else null
                    if (user == null && email.isNotBlank()) {
                        user = getUserByEmailRemote(email)
                    }
                    
                    // Fallback to local database if remote fails (e.g. offline)
                    if (user == null) {
                        user = userRepository.getUserById(authUser.userId).firstOrNull()
                    }
                    
                    // For admins, we might rely purely on email
                    if (user == null && email == "admin@gstore.com") {
                        user = User(id = "admin_123", email = email, name = "Administrator", phone = "+910000000000", role = "ADMIN")
                    }
                    if (user == null && email == "developer@gstore.com") {
                        user = User(id = "dev_456", email = email, name = "Developer Admin", phone = "+910000000000", role = "ADMIN")
                    }

                    if (user != null) {
                        withContext(Dispatchers.Main) {
                            currentUser = user
                            activeRole = user.role
                            isInitializingSession = false
                        }
                        return@launch
                    } else {
                        // Signed into Cognito but no corresponding user document in database.
                        // Force a sign out so they don't get 'Already signed in' errors on the login screen.
                        try {
                            Amplify.Auth.signOut { }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fallback to login screen
            withContext(Dispatchers.Main) {
                showLoginScreen = true
                isInitializingSession = false
            }
        }
    }

    fun customerLogin(phone: String, pass: String) {
        if (phone.isBlank() || pass.isBlank()) {
            authError = "Phone Number and Password cannot be empty"
            return
        }
        authError = null
        isNetworkLoading = true
        
        val digitsOnly = phone.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly
        val formattedPhone = if (cleanPhone.length == 10) "+91$cleanPhone" else phone.trim()

        ioScope.launch {
            try {
                // Log in via AWS Cognito directly using the phone number
                Amplify.Auth.signIn(
                    formattedPhone,
                    pass,
                    { signInResult: com.amplifyframework.auth.result.AuthSignInResult ->
                        if (signInResult.isSignedIn) {
                            Amplify.Auth.getCurrentUser(
                                { cognitoUser: com.amplifyframework.auth.AuthUser ->
                                    ioScope.launch {
                                        try {
                                            val userDoc = userRepository.getUserById(cognitoUser.userId).first()
                                            
                                            if (userDoc != null) {
                                                withContext(Dispatchers.Main) {
                                                    if (userDoc.role == "CUSTOMER") {
                                                        currentUser = userDoc
                                                        activeRole = userDoc.role
                                                        showLoginScreen = false
                                                        authError = null
                                                        isNetworkLoading = false
                                                    } else {
                                                        Amplify.Auth.signOut { }
                                                        currentUser = null
                                                        authError = "Phone number is not registered as a customer."
                                                        isNetworkLoading = false
                                                    }
                                                }
                                            } else {
                                                // Profile is missing in DynamoDB (e.g. from failed sync during registration).
                                                // Fetch attributes to reconstruct it.
                                                Amplify.Auth.fetchUserAttributes(
                                                    { attributes ->
                                                        val phoneToUse = attributes.find { it.key.keyString == "phone_number" }?.value ?: ""
                                                        val nameToUse = attributes.find { it.key.keyString == "name" }?.value ?: "Customer"
                                                        val emailToUse = attributes.find { it.key.keyString == "email" }?.value ?: ""
                                                        
                                                        val resolvedUser = User(
                                                            id = cognitoUser.userId,
                                                            phone = phoneToUse,
                                                            name = nameToUse,
                                                            role = "CUSTOMER",
                                                            email = emailToUse
                                                        )
                                                        ioScope.launch {
                                                            userRepository.saveUser(resolvedUser)
                                                            withContext(Dispatchers.Main) {
                                                                currentUser = resolvedUser
                                                                activeRole = "CUSTOMER"
                                                                showLoginScreen = false
                                                                authError = null
                                                                isNetworkLoading = false
                                                            }
                                                        }
                                                    },
                                                    {
                                                        val resolvedUser = User(
                                                            id = cognitoUser.userId,
                                                            phone = "",
                                                            name = "Customer",
                                                            role = "CUSTOMER",
                                                            email = ""
                                                        )
                                                        ioScope.launch {
                                                            userRepository.saveUser(resolvedUser)
                                                            withContext(Dispatchers.Main) {
                                                                currentUser = resolvedUser
                                                                activeRole = "CUSTOMER"
                                                                showLoginScreen = false
                                                                authError = null
                                                                isNetworkLoading = false
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            withContext(Dispatchers.Main) {
                                                authError = "Could not load profile: ${e.localizedMessage}"
                                                isNetworkLoading = false
                                            }
                                        }
                                    }
                                },
                                { error: AuthException ->
                                    ioScope.launch(Dispatchers.Main) {
                                        isNetworkLoading = false
                                        authError = "Failed to retrieve user info: ${error.message}"
                                    }
                                }
                            )
                        } else {
                            ioScope.launch(Dispatchers.Main) {
                                isNetworkLoading = false
                                authError = "Sign in incomplete. Please complete verification."
                            }
                        }
                    },
                    { error: AuthException ->
                        ioScope.launch(Dispatchers.Main) {
                            isNetworkLoading = false
                            val msg = error.toString()
                            
                            if (msg.contains("signed in", ignoreCase = true)) {
                                Amplify.Auth.signOut { }
                                authError = "Stale session detected. Please click login again."
                            } else {
                                authError = when {
                                    msg.contains("UserNotConfirmedException", ignoreCase = true) || msg.contains("not confirmed", ignoreCase = true) ->
                                        "User is not confirmed. Please confirm sign up first."
                                    msg.contains("UserNotFoundException", ignoreCase = true) || msg.contains("not found", ignoreCase = true) ->
                                        "Email is not registered. Please create an account."
                                    msg.contains("NotAuthorizedException", ignoreCase = true) || msg.contains("not authorized", ignoreCase = true) ->
                                        "Password incorrect. Please try again."
                                    else -> "Login failed: ${error.message}"
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    authError = "Unexpected error: ${e.localizedMessage}"
                    isNetworkLoading = false
                }
            }
        }
    }

    fun customerRegister(name: String, phone: String, email: String, pass: String) {
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
        if (email.isBlank() || !email.contains("@")) {
            authError = "Please enter a valid email address"
            return
        }
        val hasUpper = pass.any { it.isUpperCase() }
        val hasLower = pass.any { it.isLowerCase() }
        val hasDigit = pass.any { it.isDigit() }
        val hasSymbol = pass.any { !it.isLetterOrDigit() }
        if (pass.length < 8 || !hasUpper || !hasLower || !hasDigit || !hasSymbol) {
            authError = "Password must be at least 8 characters with uppercase, lowercase, numbers, and symbols"
            return
        }
        authError = null
        isNetworkLoading = true
        val emailTrim = email.trim()
        val formattedPhone = "+91$cleanPhone"

        // We check if this phone number or email is already registered in our remote AppSync database
        ioScope.launch {
            try {
                val phoneExists = getUserByPhoneRemote(formattedPhone) != null
                val emailExists = getUserByEmailRemote(emailTrim) != null
                
                if (phoneExists) {
                    withContext(Dispatchers.Main) {
                        authError = "An account with this phone number already exists."
                        isNetworkLoading = false
                    }
                    return@launch
                }
                if (emailExists) {
                    withContext(Dispatchers.Main) {
                        authError = "An account with this email address already exists."
                        isNetworkLoading = false
                    }
                    return@launch
                }

                // ---------------------------------------------------------------
                // BYPASS Amplify SDK for sign-up: call Cognito REST API directly
                // (Amplify internally mangles attribute names in some versions)
                // This is IDENTICAL to: aws cognito-idp sign-up --username ... 
                // ---------------------------------------------------------------
                val clientId = "ndnsac94g5m6kmm2mbk9rrvnm"
                val escapedPass = pass.replace("\\", "\\\\").replace("\"", "\\\"")
                val escapedName = name.trim().replace("\\", "\\\\").replace("\"", "\\\"")
                val jsonPayload = """{"ClientId":"$clientId","Username":"$formattedPhone","Password":"$escapedPass","UserAttributes":[{"Name":"email","Value":"$emailTrim"},{"Name":"name","Value":"$escapedName"},{"Name":"phone_number","Value":"$formattedPhone"}]}"""
                val client = okhttp3.OkHttpClient()
                val mediaType = "application/x-amz-json-1.1".toMediaTypeOrNull()
                val body = jsonPayload.toRequestBody(mediaType)
                val request = okhttp3.Request.Builder()
                    .url("https://cognito-idp.us-east-1.amazonaws.com/")
                    .post(body)
                    .addHeader("X-Amz-Target", "AWSCognitoIdentityProviderService.SignUp")
                    .addHeader("Content-Type", "application/x-amz-json-1.1")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        isNetworkLoading = false
                        authError = "Registration failed: $responseBody"
                    }
                    return@launch
                }

                // Sign-up succeeded — now sign in via Amplify to get the session
                Amplify.Auth.signIn(
                    formattedPhone,
                    pass,
                    { signInResult: com.amplifyframework.auth.result.AuthSignInResult ->
                        if (signInResult.isSignedIn) {
                            Amplify.Auth.getCurrentUser(
                                { cognitoUser: com.amplifyframework.auth.AuthUser ->
                                    val newUser = User(
                                        id = cognitoUser.userId,
                                        phone = formattedPhone,
                                        name = name.trim(),
                                        role = "CUSTOMER",
                                        email = emailTrim,
                                        pinOrPassword = ""
                                    )
                                    ioScope.launch {
                                        try { userRepository.saveUser(newUser) } catch (_: Exception) {}
                                        launch(Dispatchers.Main) {
                                            currentUser = newUser
                                            activeRole = "CUSTOMER"
                                            showLoginScreen = false
                                            isRegistering = false
                                            authError = null
                                            isNetworkLoading = false
                                        }
                                    }
                                },
                                { error: AuthException ->
                                    ioScope.launch(Dispatchers.Main) {
                                        isNetworkLoading = false
                                        authError = "Failed to retrieve user info: ${error.message}"
                                    }
                                }
                            )
                        } else {
                            ioScope.launch(Dispatchers.Main) {
                                isNetworkLoading = false
                                authError = "Auto-login failed after registration. Please log in manually."
                            }
                        }
                    },
                    { error: AuthException ->
                        ioScope.launch(Dispatchers.Main) {
                            isNetworkLoading = false
                            authError = "Sign in after registration failed: ${error.message}"
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    authError = "Unexpected error: ${e.localizedMessage}"
                    isNetworkLoading = false
                }
            }
        }
    }

    fun initiateForgotPassword(phone: String, onSuccess: () -> Unit) {
        val digitsOnly = phone.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly
        if (cleanPhone.length < 10) {
            authError = "Please enter a valid 10-digit phone number"
            return
        }
        val formattedPhone = "+91$cleanPhone"
        authError = null
        isNetworkLoading = true
        
        ioScope.launch {
            try {
                // Check if phone exists in database (userRepository) first
                val matchedUser = userRepository.getAllUsers().first().find {
                    it.phone == formattedPhone
                }
                
                if (matchedUser == null) {
                    withContext(Dispatchers.Main) {
                        authError = "This phone number is not registered. Please sign up first."
                        isNetworkLoading = false
                    }
                    return@launch
                }
                
                // If it exists, initiate reset password flow in Cognito
                Amplify.Auth.resetPassword(
                    formattedPhone,
                    { result: AuthResetPasswordResult ->
                        ioScope.launch(Dispatchers.Main) {
                            isNetworkLoading = false
                            onSuccess()
                        }
                    },
                    { error: AuthException ->
                        ioScope.launch(Dispatchers.Main) {
                            isNetworkLoading = false
                            val msg = error.message ?: ""
                            authError = when {
                                msg.contains("LimitExceededException", ignoreCase = true) ->
                                    "Attempt limit exceeded. Please try again later."
                                else -> "Failed to send reset code: ${error.message}"
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    authError = "Error: ${e.localizedMessage}"
                    isNetworkLoading = false
                }
            }
        }
    }

    fun confirmForgotPassword(phone: String, newPassword: String, code: String, onSuccess: () -> Unit) {
        val digitsOnly = phone.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly
        if (cleanPhone.length < 10) {
            authError = "Please enter a valid 10-digit phone number"
            return
        }
        val formattedPhone = "+91$cleanPhone"
        if (code.isBlank()) {
            authError = "Please enter the verification code"
            return
        }
        
        val hasUppercase = newPassword.any { it.isUpperCase() }
        val hasLowercase = newPassword.any { it.isLowerCase() }
        val hasDigit = newPassword.any { it.isDigit() }
        val hasSymbol = newPassword.any { !it.isLetterOrDigit() }
        if (newPassword.length < 8 || !hasUppercase || !hasLowercase || !hasDigit || !hasSymbol) {
            authError = "Password must be at least 8 characters and include uppercase, lowercase, numbers, and symbols"
            return
        }
        
        authError = null
        isNetworkLoading = true
        try {
            // Confirm password reset flow in AWS Cognito using phone number
            Amplify.Auth.confirmResetPassword(
                formattedPhone,
                newPassword,
                code,
                {
                    ioScope.launch(Dispatchers.Main) {
                        isNetworkLoading = false
                        onSuccess()
                    }
                },
                { error: AuthException ->
                    ioScope.launch(Dispatchers.Main) {
                        isNetworkLoading = false
                        val msg = error.message ?: ""
                        val friendlyMsg = when {
                            msg.contains("CodeMismatchException", ignoreCase = true) ||
                            msg.contains("incorrect code", ignoreCase = true) ||
                            msg.contains("invalid code", ignoreCase = true) ->
                                "The verification code is incorrect. Please check your email and try again."
                            msg.contains("ExpiredCodeException", ignoreCase = true) ||
                            msg.contains("expired", ignoreCase = true) ->
                                "The verification code has expired. Please request a new one."
                            msg.contains("InvalidPasswordException", ignoreCase = true) ||
                            msg.contains("password", ignoreCase = true) ->
                                "Password does not conform to safety requirements. Use at least 8 characters, uppercase, lowercase, numbers, and symbols."
                            else -> "Failed to reset password: ${error.message}"
                        }
                        authError = friendlyMsg
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            authError = "Error: ${e.localizedMessage}"
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
        // Cognito User Pool is configured with PHONE_NUMBER as the only usernameAttribute (no email alias).
        // Therefore, we map the admin email to the admin's phone number username for login.
        val loginUsername = when {
            emailTrim.equals("admin@gstore.com", ignoreCase = true) -> "+910000000001"
            emailTrim.equals("developer@gstore.com", ignoreCase = true) -> "+910000000002"
            else -> emailTrim
        }

        try {
            // Log in via AWS Cognito
            Amplify.Auth.signIn(
                loginUsername,
                password,
                { signInResult: com.amplifyframework.auth.result.AuthSignInResult ->
                    if (signInResult.isSignedIn) {
                        Amplify.Auth.getCurrentUser(
                            { cognitoUser: com.amplifyframework.auth.AuthUser ->
                                ioScope.launch {
                                    try {
                                        val userDoc = userRepository.getUserById(cognitoUser.userId).first()
                                        withContext(Dispatchers.Main) {
                                            val isDev = emailTrim.equals("developer@gstore.com", ignoreCase = true)
                                            val resolvedUser = userDoc ?: User(
                                                id = cognitoUser.userId,
                                                phone = if (isDev) "+910000000002" else "+910000000001",
                                                name = if (isDev) "G-STORE Dev Admin" else "G-STORE Admin",
                                                role = "ADMIN",
                                                email = emailTrim,
                                                pinOrPassword = ADMIN_PASSWORD
                                            ).also {
                                                ioScope.launch { userRepository.saveUser(it) }
                                            }
                                            if (resolvedUser.role == "ADMIN") {
                                                currentUser = resolvedUser
                                                activeRole = resolvedUser.role
                                                showLoginScreen = false
                                                authError = null
                                                isNetworkLoading = false
                                            } else {
                                                Amplify.Auth.signOut { }
                                                currentUser = null
                                                authError = "You are not registered as an administrator."
                                                isNetworkLoading = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) {
                                            authError = "Could not load admin profile: ${e.localizedMessage}"
                                            isNetworkLoading = false
                                        }
                                    }
                                }
                            },
                            { error: AuthException ->
                                ioScope.launch(Dispatchers.Main) {
                                    isNetworkLoading = false
                                    authError = "Failed to retrieve user info: ${error.message}"
                                }
                            }
                        )
                    } else {
                        ioScope.launch(Dispatchers.Main) {
                            isNetworkLoading = false
                            authError = "Admin sign in incomplete."
                        }
                    }
                },
                { error: AuthException ->
                    ioScope.launch(Dispatchers.Main) {
                        isNetworkLoading = false
                        val msg = error.toString()
                        
                        if (msg.contains("signed in", ignoreCase = true)) {
                            Amplify.Auth.signOut { }
                            authError = "Stale session detected. Please click login again."
                        } else {
                            authError = when {
                                msg.contains("UserNotFoundException", ignoreCase = true) || msg.contains("not found", ignoreCase = true) ->
                                    "Admin email is not registered."
                                msg.contains("NotAuthorizedException", ignoreCase = true) || msg.contains("not authorized", ignoreCase = true) ->
                                    "Password incorrect. Please try again."
                                else -> "Admin login failed: ${error.message}"
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            authError = "Unexpected error: ${e.localizedMessage}"
            isNetworkLoading = false
        }   }

    fun logout() {
        try { Amplify.Auth.signOut { } } catch (_: Exception) {}
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

    private var categoriesJob: Job? = null
    private var giftsJob: Job? = null

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

    private fun observeCategories() {
        categoriesJob?.cancel()
        categoriesJob = ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    productRepository.getAllCategories().collect { list ->
                        withContext(Dispatchers.Main) {
                            categoriesList = list
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeGifts() {
        giftsJob?.cancel()
        giftsJob = ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    productRepository.getGiftConfigs().collect { list ->
                        withContext(Dispatchers.Main) {
                            giftConfigsList = list
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addNewCategory(nameEn: String, imageUrl: String) {
        ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    val newId = "c_${UUID.randomUUID()}"
                    val newCategory = Category(
                        id = newId,
                        nameEn = nameEn,
                        imageUrl = imageUrl.ifBlank { "android.resource://com.aistudio.ricemart.pkqmsx/drawable/rice_bags_preview" }
                    )
                    val updated = categoriesList + newCategory
                    productRepository.saveCategories(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addNewGiftConfig(thresholdAmount: Double, giftPrice: Double, productName: String, imageUrl: String, stockQuantity: Int) {
        ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    val newId = "g_${UUID.randomUUID()}"
                    val newGift = GiftItemConfig(
                        id = newId,
                        thresholdAmount = thresholdAmount,
                        giftPrice = giftPrice,
                        productName = productName,
                        imageUrl = imageUrl,
                        stockQuantity = stockQuantity
                    )
                    val updated = giftConfigsList + newGift
                    productRepository.saveGiftConfigs(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteGiftConfig(id: String) {
        ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    val updated = giftConfigsList.filter { it.id != id }
                    productRepository.saveGiftConfigs(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshProductsFromCloud() {
        ioScope.launch {
            try {
                if (::productRepository.isInitialized) {
                    productRepository.forceRefreshFromCloud()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seedTestData(onComplete: () -> Unit = {}) {
        ioScope.launch {
            try {
                // Delete all current products
                val current = productsList.toList()
                current.forEach { prod ->
                    productRepository.deleteProduct(prod.id)
                }

                // Delete system categories and write default categories
                val defaultCats = listOf(
                    Category(id = "c_rice", nameEn = "Rice Bags", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/rice_bags_preview"),
                    Category(id = "c_dal", nameEn = "Dals & Pulses", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/dals_pulses_preview"),
                    Category(id = "c_oil", nameEn = "Cooking Oils", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/cooking_oils_preview"),
                    Category(id = "c_dairy", nameEn = "Dairy Essentials", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/dairy_essentials_preview"),
                    Category(id = "c_spices", nameEn = "Spices & Masalas", imageUrl = "android.resource://com.aistudio.ricemart.pkqmsx/drawable/spices_masalas_preview")
                )
                productRepository.saveCategories(defaultCats)

                // 15 products (3 per category)
                val testProducts = listOf(
                    // RICE BAGS (c_rice)
                    Product(
                        id = "p_rice_sona",
                        categoryId = "c_rice",
                        nameEn = "Premium Sona Masuri Rice",
                        nameTe = "సోనా మసూరి బియ్యం",
                        brand = "Akshaya",
                        descriptionEn = "Aged premium Sona Masuri rice, ideal for daily cooking. Clean and raw grains with rich aroma.",
                        descriptionTe = "",
                        shortDescriptionEn = "Aged premium Sona Masuri rice.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/rice_bags_preview"),
                        variants = listOf(
                            ProductVariant("v_rice_sona_26", "26", "kg", 1350.0, 1500.0, 50, "SKU-R-SONA-26"),
                            ProductVariant("v_rice_sona_10", "10", "kg", 550.0, 600.0, 100, "SKU-R-SONA-10"),
                            ProductVariant("v_rice_sona_5", "5", "kg", 300.0, 320.0, 120, "SKU-R-SONA-5")
                        )
                    ),
                    Product(
                        id = "p_rice_basmati",
                        categoryId = "c_rice",
                        nameEn = "Royal Basmati Rice",
                        nameTe = "బాస్మతి బియ్యం",
                        brand = "Sameera",
                        descriptionEn = "Long grain aromatic Basmati rice, perfect for Biryanis and Pulaos.",
                        descriptionTe = "",
                        shortDescriptionEn = "Long grain aromatic Basmati rice.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/rice_bags_preview"),
                        variants = listOf(
                            ProductVariant("v_rice_bas_5", "5", "kg", 600.0, 750.0, 30, "SKU-R-BAS-5"),
                            ProductVariant("v_rice_bas_1", "1", "kg", 130.0, 160.0, 80, "SKU-R-BAS-1")
                        )
                    ),
                    Product(
                        id = "p_rice_lalitha",
                        categoryId = "c_rice",
                        nameEn = "Lalitha Brand HMT Rice",
                        nameTe = "లలిత బ్రాండ్ బియ్యం",
                        brand = "Lalitha",
                        descriptionEn = "Finest quality HMT Kolam rice from Lalitha brand. Soft texture and easily digestible.",
                        descriptionTe = "",
                        shortDescriptionEn = "Finest quality HMT Kolam rice.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/rice_bags_preview"),
                        variants = listOf(
                            ProductVariant("v_rice_lal_26", "26", "kg", 1400.0, 1600.0, 40, "SKU-R-LAL-26"),
                            ProductVariant("v_rice_lal_10", "10", "kg", 580.0, 650.0, 90, "SKU-R-LAL-10")
                        )
                    ),

                    // DALS & PULSES (c_dal)
                    Product(
                        id = "p_dal_toor",
                        categoryId = "c_dal",
                        nameEn = "Premium Toor Dal",
                        nameTe = "కందిపప్పు",
                        brand = "G-Store",
                        descriptionEn = "High-quality split pigeon peas (Toor Dal), unpolished and rich in protein.",
                        descriptionTe = "",
                        shortDescriptionEn = "High-quality split pigeon peas.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/dals_pulses_preview"),
                        variants = listOf(
                            ProductVariant("v_dal_toor_1", "1", "kg", 160.0, 180.0, 150, "SKU-D-TOOR-1"),
                            ProductVariant("v_dal_toor_500g", "500", "g", 85.0, 95.0, 200, "SKU-D-TOOR-500G")
                        )
                    ),
                    Product(
                        id = "p_dal_moong",
                        categoryId = "c_dal",
                        nameEn = "Organic Yellow Moong Dal",
                        nameTe = "పెసరపప్పు",
                        brand = "G-Store",
                        descriptionEn = "Nutritious and easy-to-cook split yellow mung beans, organically processed.",
                        descriptionTe = "",
                        shortDescriptionEn = "Nutritious yellow moong dal.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/dals_pulses_preview"),
                        variants = listOf(
                            ProductVariant("v_dal_moong_1", "1", "kg", 140.0, 160.0, 120, "SKU-D-MOONG-1"),
                            ProductVariant("v_dal_moong_500g", "500", "g", 75.0, 85.0, 180, "SKU-D-MOONG-500G")
                        )
                    ),
                    Product(
                        id = "p_dal_chana",
                        categoryId = "c_dal",
                        nameEn = "Chana Dal Special",
                        nameTe = "శనగపప్పు",
                        brand = "G-Store",
                        descriptionEn = "Cleaned and sorted split Bengal gram (Chana Dal), delicious for curry and snacks.",
                        descriptionTe = "",
                        shortDescriptionEn = "Cleaned split Bengal gram.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/dals_pulses_preview"),
                        variants = listOf(
                            ProductVariant("v_dal_chana_1", "1", "kg", 110.0, 130.0, 140, "SKU-D-CHANA-1"),
                            ProductVariant("v_dal_chana_500g", "500", "g", 60.0, 70.0, 220, "SKU-D-CHANA-500G")
                        )
                    ),

                    // COOKING OILS (c_oil)
                    Product(
                        id = "p_oil_sunflower",
                        categoryId = "c_oil",
                        nameEn = "Fortune Sunflower Oil",
                        nameTe = "సన్‌ఫ్లవర్ ఆయిల్",
                        brand = "Fortune",
                        descriptionEn = "Light and healthy refined sunflower oil, rich in vitamins. Perfect for deep frying.",
                        descriptionTe = "",
                        shortDescriptionEn = "Light refined sunflower oil.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/cooking_oils_preview"),
                        variants = listOf(
                            ProductVariant("v_oil_sun_1", "1", "L", 120.0, 140.0, 100, "SKU-O-SUN-1"),
                            ProductVariant("v_oil_sun_5", "5", "L", 580.0, 650.0, 50, "SKU-O-SUN-5")
                        )
                    ),
                    Product(
                        id = "p_oil_groundnut",
                        categoryId = "c_oil",
                        nameEn = "Cold-Pressed Groundnut Oil",
                        nameTe = "веరుశనగ నూనె",
                        brand = "G-Store",
                        descriptionEn = "100% natural cold-pressed groundnut oil, retains nutrients and sweet aroma.",
                        descriptionTe = "",
                        shortDescriptionEn = "Natural cold-pressed groundnut oil.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/cooking_oils_preview"),
                        variants = listOf(
                            ProductVariant("v_oil_grnd_1", "1", "L", 190.0, 210.0, 80, "SKU-O-GRND-1"),
                            ProductVariant("v_oil_grnd_5", "5", "L", 920.0, 1000.0, 40, "SKU-O-GRND-5")
                        )
                    ),
                    Product(
                        id = "p_oil_mustard",
                        categoryId = "c_oil",
                        nameEn = "Fortune Kachi Ghani Mustard Oil",
                        nameTe = "ఆవ నూనె",
                        brand = "Fortune",
                        descriptionEn = "Strong aroma kachi ghani mustard oil, ideal for traditional pickles and cooking.",
                        descriptionTe = "",
                        shortDescriptionEn = "Kachi ghani mustard oil.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/cooking_oils_preview"),
                        variants = listOf(
                            ProductVariant("v_oil_must_1", "1", "L", 150.0, 170.0, 90, "SKU-O-MUST-1")
                        )
                    ),

                    // DAIRY ESSENTIALS (c_dairy)
                    Product(
                        id = "p_dairy_ghee",
                        categoryId = "c_dairy",
                        nameEn = "Amul Pure Cow Ghee",
                        nameTe = "ఆవు నెయ్యి",
                        brand = "Amul",
                        descriptionEn = "Pure and aromatic cow ghee from Amul, enhances taste and aids digestion.",
                        descriptionTe = "",
                        shortDescriptionEn = "Pure and aromatic cow ghee.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/dairy_essentials_preview"),
                        variants = listOf(
                            ProductVariant("v_dairy_ghee_1", "1", "L", 680.0, 720.0, 60, "SKU-DY-GHEE-1"),
                            ProductVariant("v_dairy_ghee_500", "500", "ml", 350.0, 380.0, 100, "SKU-DY-GHEE-500")
                        )
                    ),
                    Product(
                        id = "p_dairy_paneer",
                        categoryId = "c_dairy",
                        nameEn = "Amul Fresh Paneer",
                        nameTe = "పన్నీర్",
                        brand = "Amul",
                        descriptionEn = "Soft and delicious fresh paneer cubes, rich in calcium and protein.",
                        descriptionTe = "",
                        shortDescriptionEn = "Soft fresh paneer cubes.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/dairy_essentials_preview"),
                        variants = listOf(
                            ProductVariant("v_dairy_pan_500", "500", "g", 240.0, 260.0, 50, "SKU-DY-PAN-500"),
                            ProductVariant("v_dairy_pan_200", "200", "g", 100.0, 110.0, 120, "SKU-DY-PAN-200")
                        )
                    ),
                    Product(
                        id = "p_dairy_curd",
                        categoryId = "c_dairy",
                        nameEn = "Amul Masti Curd",
                        nameTe = "పెరుగు",
                        brand = "Amul",
                        descriptionEn = "Pasteurized thick curd, fresh and hygienic daily essential.",
                        descriptionTe = "",
                        shortDescriptionEn = "Pasteurized thick curd.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/dairy_essentials_preview"),
                        variants = listOf(
                            ProductVariant("v_dairy_crd_1", "1", "kg", 90.0, 100.0, 70, "SKU-DY-CRD-1"),
                            ProductVariant("v_dairy_crd_500", "500", "g", 50.0, 55.0, 140, "SKU-DY-CRD-500")
                        )
                    ),

                    // SPICES & MASALAS (c_spices)
                    Product(
                        id = "p_spices_turmeric",
                        categoryId = "c_spices",
                        nameEn = "Organic Turmeric Powder",
                        nameTe = "పసుపు",
                        brand = "G-Store",
                        descriptionEn = "Pure organic turmeric powder, high curcumin content with golden color.",
                        descriptionTe = "",
                        shortDescriptionEn = "Pure organic turmeric powder.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/spices_masalas_preview"),
                        variants = listOf(
                            ProductVariant("v_spices_tur_500", "500", "g", 90.0, 110.0, 100, "SKU-S-TUR-500"),
                            ProductVariant("v_spices_tur_200", "200", "g", 45.0, 55.0, 200, "SKU-S-TUR-200")
                        )
                    ),
                    Product(
                        id = "p_spices_chili",
                        categoryId = "c_spices",
                        nameEn = "Kashmiri Red Chili Powder",
                        nameTe = "కారం పొడి",
                        brand = "G-Store",
                        descriptionEn = "Bright red Kashmiri chili powder, mildly hot with rich natural coloring properties.",
                        descriptionTe = "",
                        shortDescriptionEn = "Bright red Kashmiri chili powder.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/spices_masalas_preview"),
                        variants = listOf(
                            ProductVariant("v_spices_chl_500", "500", "g", 140.0, 160.0, 80, "SKU-S-CHL-500"),
                            ProductVariant("v_spices_chl_200", "200", "g", 65.0, 75.0, 150, "SKU-S-CHL-200")
                        )
                    ),
                    Product(
                        id = "p_spices_garam",
                        categoryId = "c_spices",
                        nameEn = "Premium Garam Masala",
                        nameTe = "గరం మసాలా",
                        brand = "G-Store",
                        descriptionEn = "Handcrafted blend of aromatic whole spices, freshly roasted and ground.",
                        descriptionTe = "",
                        shortDescriptionEn = "Aromatic blend of whole spices.",
                        shortDescriptionTe = "",
                        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/spices_masalas_preview"),
                        variants = listOf(
                            ProductVariant("v_spices_gar_200", "200", "g", 120.0, 140.0, 90, "SKU-S-GAR-200"),
                            ProductVariant("v_spices_gar_100", "100", "g", 65.0, 75.0, 160, "SKU-S-GAR-100")
                        )
                    )
                )

                testProducts.forEach { prod ->
                    productRepository.saveProduct(prod)
                }

                // Force refresh locally and trigger callback
                productRepository.forceRefreshFromCloud()
                launch(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    onComplete()
                }
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

        // Switched from Firebase to AWS Amplify Repositories
        productRepository = com.example.data.repository.AwsProductRepositoryImpl(context)
        addressRepository = com.example.data.repository.AwsAddressRepositoryImpl(context)
        orderRepository = com.example.data.repository.AwsOrderRepositoryImpl(context)
        userRepository = com.example.data.repository.AwsUserRepositoryImpl(context)

        // Start observing products immediately (succeeds if Firestore rules allow public reads)
        observeProducts()
        observeCategories()

        ioScope.launch {
            // Restore active AWS Cognito session if exists
            try {
                Amplify.Auth.getCurrentUser(
                    { cognitoUser: com.amplifyframework.auth.AuthUser ->
                        ioScope.launch {
                            try {
                                val userDoc = userRepository.getUserById(cognitoUser.userId).first()
                                val resolvedUser = userDoc ?: User(
                                    id = cognitoUser.userId,
                                    phone = "+91 00000 00000",
                                    name = "Customer",
                                    role = "CUSTOMER",
                                    email = ""
                                ).also {
                                    userRepository.saveUser(it)
                                }
                                withContext(Dispatchers.Main) {
                                    currentUser = resolvedUser
                                    activeRole = resolvedUser.role
                                    showLoginScreen = false
                                    authError = null
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    currentUser = null
                                    showLoginScreen = true
                                }
                            }
                        }
                    },
                    { error: AuthException ->
                        // Not logged in or error restoring session
                        ioScope.launch(Dispatchers.Main) {
                            currentUser = null
                            showLoginScreen = true
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
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

    suspend fun placeOrder(selectedGiftId: String? = null): String? = withContext(Dispatchers.IO) {
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

        var finalTotalAmount = cartTotal
        val selectedGift = giftConfigsList.find { it.id == selectedGiftId }
        var giftToUpdate: GiftItemConfig? = null
        if (selectedGift != null) {
            if (selectedGift.stockQuantity > 0) {
                currentItems.add(OrderItem(
                    productId = selectedGift.id,
                    productName = selectedGift.productName,
                    selectedSize = "Gift",
                    priceAtPurchase = selectedGift.giftPrice,
                    quantity = 1,
                    isGift = true
                ))
                finalTotalAmount += selectedGift.giftPrice
                giftToUpdate = selectedGift.copy(stockQuantity = selectedGift.stockQuantity - 1)
            }
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
            totalAmount = finalTotalAmount,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            items = currentItems
        )

        try {
            val cartSnapshot = cartItems.toMap()
            val uniqueProductIds = cartSnapshot.keys.map { it.split("#")[0] }.distinct()

            // 1. Fetch fresh products from the repository database
            val dbProducts = uniqueProductIds.associateWith { prodId ->
                productRepository.getProductById(prodId) ?: throw Exception("Product not found in store catalog.")
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

            // 3. Write updated product stock records to repository
            productsToUpdate.forEach { prod ->
                productRepository.saveProduct(prod)
            }
            
            if (giftToUpdate != null) {
                val updatedGiftList = giftConfigsList.map { if (it.id == giftToUpdate.id) giftToUpdate else it }
                productRepository.saveGiftConfigs(updatedGiftList)
            }

            // 4. Save the order to repository
            val orderToSave = newOrder.copy(id = newOrder.id.ifEmpty { "G-${System.currentTimeMillis()}-${(100..999).random()}" })
            orderRepository.saveOrder(orderToSave)

            withContext(Dispatchers.Main) {
                lastPlacedOrder = orderToSave
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
        variants: List<ProductVariant>,
        categoryId: String = "c_rice"
    ) {
        val newProd = Product(
            id = "p_${System.currentTimeMillis()}",
            categoryId = categoryId,
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

    /** Customer can cancel an order only while it is still PENDING (not yet dispatched). */
    fun customerCancelOrder(orderId: String) {
        val order = ordersList.find { it.id == orderId } ?: return
        if (order.status != OrderStatus.PENDING) return
        updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    /** Customer can request a return only after the order has been DELIVERED. */
    fun customerRequestReturn(orderId: String) {
        val order = ordersList.find { it.id == orderId } ?: return
        if (order.status != OrderStatus.DELIVERED) return
        updateOrderStatus(orderId, OrderStatus.RETURN_REQUESTED)
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
