package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.state.AppState
import com.example.ui.theme.RoyalEmerald

val SurfaceEmerald = Color(0xFFF1F7F5) // Soft emerald tint for background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    // Form Inputs
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var loginPhoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmailInput by remember { mutableStateOf("") }
    var forgotPhoneInput by remember { mutableStateOf("") }
    var forgotCodeInput by remember { mutableStateOf("") }
    var forgotNewPasswordInput by remember { mutableStateOf("") }
    var forgotStep by remember { mutableStateOf(1) }
 
    // Admin Inputs
    var adminEmailInput by remember { mutableStateOf("") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminPasswordVisible by remember { mutableStateOf(false) }
    
    // Secret Tap Trick
    var tapCount by remember { mutableStateOf(0) }
    var showAdminLogin by remember { mutableStateOf(false) }

    val primaryGreen = RoyalEmerald

    // Reusable colors for text fields
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = primaryGreen,
        focusedBorderColor = primaryGreen,
        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
        focusedLabelColor = primaryGreen,
        unfocusedLabelColor = Color.Gray,
        focusedLeadingIconColor = primaryGreen,
        unfocusedLeadingIconColor = Color.Gray,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceEmerald)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Premium Brand Header with 5-tap Admin toggle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(primaryGreen)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        tapCount++
                        if (tapCount >= 5) {
                            showAdminLogin = !showAdminLogin
                            tapCount = 0
                            AppState.authError = null
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.gstore_logo_transparent),
                    contentDescription = "G-STORE Logo",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (showAdminLogin) "G-STORE ADMIN" else "G-STORE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = primaryGreen,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    AnimatedVisibility(visible = AppState.authError != null) {
                        AppState.authError?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!showAdminLogin) { // Customer Flow
                        if (AppState.isRegistering) {
                            // --- REGISTRATION FORM ---
                            Text(
                                text = "Full Name",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter your name", color = Color.Gray) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = primaryGreen)
                                },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = fieldColors
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Phone Number",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { if (it.length <= 10) phoneInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("10 digit number", color = Color.Gray) },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = primaryGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+91", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = fieldColors
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Email Address",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("email@example.com", color = Color.Gray) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Email, contentDescription = null, tint = primaryGreen)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = fieldColors
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("••••••••", color = Color.Gray) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = primaryGreen)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = "Toggle password visibility",
                                            tint = primaryGreen
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = fieldColors
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "At least 8 characters with letters, numbers, and symbols",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { AppState.customerRegister(nameInput, phoneInput, emailInput, passwordInput) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                enabled = !AppState.isNetworkLoading
                            ) {
                                if (AppState.isNetworkLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = "Create Account",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = buildAnnotatedString {
                                    append("Already have an account? ")
                                    withStyle(style = SpanStyle(color = primaryGreen, fontWeight = FontWeight.Bold)) {
                                        append("Login")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        AppState.isRegistering = false
                                        AppState.authError = null
                                    },
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // --- LOGIN FORM ---
                            Text(
                                text = "Phone Number",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = loginPhoneInput,
                                onValueChange = { if (it.length <= 10) loginPhoneInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("10 digit number", color = Color.Gray) },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = primaryGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+91", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("••••••••", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = primaryGreen) },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility",
                                            tint = primaryGreen
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen,
                                    modifier = Modifier.clickable {
                                        forgotEmailInput = ""
                                        forgotPhoneInput = ""
                                        forgotCodeInput = ""
                                        forgotNewPasswordInput = ""
                                        forgotStep = 1
                                        showForgotPasswordDialog = true
                                        AppState.authError = null
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { AppState.customerLogin(loginPhoneInput, passwordInput) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !AppState.isNetworkLoading
                            ) {
                                if (AppState.isNetworkLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Don't have an account? ")
                                    withStyle(style = SpanStyle(color = primaryGreen, fontWeight = FontWeight.Bold)) {
                                        append("Register")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        AppState.isRegistering = true
                                        AppState.authError = null
                                    },
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else { // Admin Flow
                        Text(
                            text = "Admin Email",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adminEmailInput,
                            onValueChange = { adminEmailInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter admin email") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = primaryGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Admin Password",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = { adminPasswordInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = primaryGreen) },
                            trailingIcon = {
                                IconButton(onClick = { adminPasswordVisible = !adminPasswordVisible }) {
                                    Icon(
                                        imageVector = if (adminPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = primaryGreen
                                    )
                                }
                            },
                            visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = fieldColors
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { AppState.loginAsAdmin(adminEmailInput, adminPasswordInput) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !AppState.isNetworkLoading
                        ) {
                            if (AppState.isNetworkLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Admin Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = {
                    Text(
                        text = if (forgotStep == 1) "Forgot Password" else "Reset Password",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (AppState.authError != null) {
                            Text(
                                text = AppState.authError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (forgotStep == 1) {
                            Text(
                                text = "Enter your registered phone number. We will send a password reset verification code to your registered email address.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            OutlinedTextField(
                                value = forgotPhoneInput,
                                onValueChange = { if (it.length <= 10) forgotPhoneInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("10 digit number", color = Color.Gray) },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = primaryGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+91", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                        } else {
                            Text(
                                text = "A verification code has been sent to your registered email. Enter the code and your new password below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            OutlinedTextField(
                                value = forgotCodeInput,
                                onValueChange = { forgotCodeInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter verification code") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = primaryGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            OutlinedTextField(
                                value = forgotNewPasswordInput,
                                onValueChange = { forgotNewPasswordInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("New Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = primaryGreen) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (forgotStep == 1) {
                                AppState.initiateForgotPassword(forgotPhoneInput) {
                                    forgotStep = 2
                                    AppState.authError = null
                                }
                            } else {
                                AppState.confirmForgotPassword(forgotPhoneInput, forgotNewPasswordInput, forgotCodeInput) {
                                    showForgotPasswordDialog = false
                                    AppState.authError = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !AppState.isNetworkLoading
                    ) {
                        if (AppState.isNetworkLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (forgotStep == 1) "Send Code" else "Reset Password", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
