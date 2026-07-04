package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.state.AppState
import com.example.ui.theme.RoyalEmerald

@Composable
fun LoginScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0: Customer, 1: Admin

    // Form Inputs
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginPasswordVisible by remember { mutableStateOf(false) }
 
    // Admin Inputs
    var adminEmailInput by remember { mutableStateOf("") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminPasswordVisible by remember { mutableStateOf(false) }

    val primaryGreen = RoyalEmerald

    // Reusable colors for ALL text fields — guarantees black text on white background
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = primaryGreen,
        focusedBorderColor = primaryGreen,
        unfocusedBorderColor = Color.LightGray,
        focusedLabelColor = primaryGreen,
        unfocusedLabelColor = Color.Gray,
        focusedLeadingIconColor = primaryGreen,
        unfocusedLeadingIconColor = Color.Gray,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Premium Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(primaryGreen, Color(0xFF022C22))
                    ),
                    shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("G-STORE", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Premium Quality • Fresh Delivery", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Login Card
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-30).dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Role Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = primaryGreen,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = primaryGreen
                        )
                    }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; AppState.authError = null }) {
                        Text("Customer", modifier = Modifier.padding(12.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; AppState.authError = null }) {
                        Text("Admin", modifier = Modifier.padding(12.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = AppState.authError != null) {
                    AppState.authError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (selectedTab == 0) { // Customer Flow
                    if (AppState.isRegistering) {
                        // Registration Form
                        RequiredLabel("Full Name")
                        OutlinedTextField(
                            value = nameInput, onValueChange = { nameInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = primaryGreen) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RequiredLabel("Phone Number")
                        OutlinedTextField(
                            value = phoneInput, onValueChange = { if (it.length <= 10) phoneInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("10 digit number") },
                            prefix = { Text("+91 ", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = primaryGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RequiredLabel("Email Address")
                        OutlinedTextField(
                            value = emailInput, onValueChange = { emailInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("example@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = primaryGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RequiredLabel("Password")
                        OutlinedTextField(
                            value = passwordInput, onValueChange = { passwordInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("At least 6 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = primaryGreen) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = primaryGreen
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { AppState.customerRegister(nameInput, phoneInput, emailInput, passwordInput) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !AppState.isNetworkLoading
                        ) {
                            if (AppState.isNetworkLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        TextButton(onClick = { AppState.isRegistering = false; AppState.authError = null }) {
                            Text("Already have an account? Login", color = primaryGreen, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Email Login Form
                        RequiredLabel("Email Address")
                        OutlinedTextField(
                            value = emailInput, onValueChange = { emailInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("example@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = primaryGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RequiredLabel("Password")
                        OutlinedTextField(
                            value = passwordInput, onValueChange = { passwordInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your password") },
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
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { AppState.customerLogin(emailInput, passwordInput) },
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
                        TextButton(onClick = { AppState.isRegistering = true; AppState.authError = null }) {
                            Text("Don't have an account? Register", color = primaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                } else { // Admin Flow
                    RequiredLabel("Admin Email")
                    OutlinedTextField(
                        value = adminEmailInput, onValueChange = { adminEmailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter admin email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = primaryGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = fieldColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RequiredLabel("Admin Password")
                    OutlinedTextField(
                        value = adminPasswordInput, onValueChange = { adminPasswordInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter password") },
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
}

@Composable
fun RequiredLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF424242),
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}
