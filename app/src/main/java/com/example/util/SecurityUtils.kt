package com.example.util

import java.security.MessageDigest

/**
 * Lightweight cryptographic utility for Android app security.
 * Provides SHA-256 hashing and constant-time password verification without external dependencies.
 */
object SecurityUtils {

    /**
     * Hashes a password or PIN string using SHA-256.
     * Returns an empty string if input is blank.
     */
    fun hashPassword(password: String): String {
        if (password.isBlank()) return ""
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that the provided raw password matches the expected SHA-256 hash.
     * Uses constant-time character comparison to prevent timing attacks.
     */
    fun verifyPassword(rawPassword: String, expectedHash: String): Boolean {
        if (rawPassword.isBlank() || expectedHash.isBlank()) return false
        val computedHash = hashPassword(rawPassword)
        return constantTimeEquals(computedHash, expectedHash)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
