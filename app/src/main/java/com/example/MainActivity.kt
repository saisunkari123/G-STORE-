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
import com.example.ui.admin.AdminScreen
import com.example.ui.customer.CustomerScreen
import com.example.ui.state.AppState
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { AppState.isInitializingSession }

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
            if (AppState.showLoginScreen) {
                com.example.ui.auth.LoginScreen()
            } else {
                when (AppState.activeRole) {
                  "CUSTOMER" -> CustomerScreen()
                  "ADMIN" -> AdminScreen()
                  else -> CustomerScreen()
                }
            }
          }
        }
      }
    }
  }
}
