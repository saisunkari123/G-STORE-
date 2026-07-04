package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.ui.admin.AdminScreen
import com.example.ui.customer.CustomerScreen
import com.example.ui.state.AppState
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppState.initializeDatabase(applicationContext)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            if (AppState.showLoginScreen) {
                com.example.ui.auth.LoginScreen()
            } else {
                when (AppState.activeRole) {
                  "CUSTOMER" -> CustomerScreen()
                  "ADMIN" -> AdminScreen()
                  // DELIVERY_BOY role is not implemented (local business — no riders).
                  // Fallback to CustomerScreen to prevent a blank screen.
                  else -> CustomerScreen()
                }
            }
          }
        }
      }
    }
  }
}
