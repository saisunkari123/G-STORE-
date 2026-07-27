package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ConnectException
import java.net.UnknownHostException

class NetworkUtilsTest {

    @Test
    fun `isOfflineError detects UnknownHostException`() {
        val ex = UnknownHostException("api.cloudinary.com: UnknownHostException")
        assertTrue("UnknownHostException should be identified as offline error", NetworkUtils.isOfflineError(ex))
    }

    @Test
    fun `isOfflineError detects ConnectException`() {
        val ex = ConnectException("Failed to connect to cognito-idp.us-east-1.amazonaws.com")
        assertTrue("ConnectException should be identified as offline error", NetworkUtils.isOfflineError(ex))
    }

    @Test
    fun `getFriendlyAuthErrorMessage returns friendly offline text when offline`() {
        val ex = UnknownHostException("Unable to resolve host")
        val message = NetworkUtils.getFriendlyAuthErrorMessage(ex, "Login")
        assertEquals("Device is offline. Please check your internet connection.", message)
    }
}
