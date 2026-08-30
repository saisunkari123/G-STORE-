package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.location.AWSLocationGeoPlugin
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.ui.SplashScreen
import com.example.ui.admin.AdminScreen
import com.example.ui.customer.CustomerScreen
import com.example.ui.state.AppState
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // Dismiss the system splash immediately — Compose will handle the splash screen with real text
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { false }

    super.onCreate(savedInstanceState)
    try {
      Amplify.addPlugin(AWSApiPlugin())
      Amplify.addPlugin(AWSCognitoAuthPlugin())
      Amplify.addPlugin(AWSS3StoragePlugin())
      Amplify.addPlugin(AWSLocationGeoPlugin())
      Amplify.configure(applicationContext)
      Log.i("AmplifyInit", "Initialized Amplify successfully")
    } catch (error: AmplifyException) {
      Log.e("AmplifyInit", "Could not initialize Amplify", error)
    }
    AppState.initializeDatabase(applicationContext)
    try {
      org.osmdroid.config.Configuration.getInstance().load(applicationContext, applicationContext.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
      org.osmdroid.config.Configuration.getInstance().userAgentValue = applicationContext.packageName
    } catch (_: Exception) {}
    AppState.restoreSession()
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
            if (AppState.isInitializingSession) {
              // Show Compose splash (pure text, no image box) while session loads
              SplashScreen()
            } else if (AppState.showLoginScreen) {
              com.example.ui.auth.LoginScreen()
            } else {
              when (AppState.activeRole) {
                "CUSTOMER" -> CustomerScreen()
                "ADMIN" -> AdminScreen()
                "DELIVERY" -> com.example.ui.delivery.DeliveryScreen()
                else -> CustomerScreen()
              }
            }
          }
        }
      }
    }
  }
}
