package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {

    /**
     * Checks if the device has an active internet connection.
     */
    fun isOnline(context: Context?): Boolean {
        if (context == null) return true
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Inspects an Exception / Throwable chain to detect if it is an offline network error.
     */
    fun isOfflineError(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        var current: Throwable? = throwable
        while (current != null) {
            val msg = (current.message ?: "").lowercase() + " " + current.javaClass.name.lowercase() + " " + current.toString().lowercase()
            if (msg.contains("unknownhostexception") ||
                msg.contains("connectexception") ||
                msg.contains("sockettimeoutexception") ||
                msg.contains("nonetworkexception") ||
                msg.contains("unable to resolve host") ||
                msg.contains("failed to connect") ||
                msg.contains("networkexception") ||
                msg.contains("no internet") ||
                msg.contains("signedoutexception") ||
                msg.contains("network error")
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    /**
     * Returns a user-friendly error message for auth and network operations.
     */
    fun getFriendlyAuthErrorMessage(throwable: Throwable?, defaultActionName: String = "Login"): String {
        if (isOfflineError(throwable)) {
            return "Device is offline. Please check your internet connection."
        }
        val msg = throwable?.toString() ?: ""
        return when {
            msg.contains("UserNotConfirmedException", ignoreCase = true) || msg.contains("not confirmed", ignoreCase = true) ->
                "Account is not confirmed. Please complete verification."
            msg.contains("UserNotFoundException", ignoreCase = true) || msg.contains("not found", ignoreCase = true) ->
                "This mobile number is not registered. Please create an account."
            msg.contains("NotAuthorizedException", ignoreCase = true) || msg.contains("not authorized", ignoreCase = true) ->
                "Incorrect mobile number or password. Please check your details and try again."
            msg.contains("UsernameExistsException", ignoreCase = true) || msg.contains("already exists", ignoreCase = true) ->
                "An account with this mobile number already exists. Please log in."
            msg.contains("InvalidParameterException", ignoreCase = true) || msg.contains("invalid parameter", ignoreCase = true) ->
                "Please enter a valid 10-digit mobile number and password."
            else -> "$defaultActionName failed: ${throwable?.localizedMessage ?: "Unknown error"}"
        }
    }
}
