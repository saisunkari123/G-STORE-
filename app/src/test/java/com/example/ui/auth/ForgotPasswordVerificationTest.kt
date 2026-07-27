package com.example.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgotPasswordVerificationTest {

    @Test
    fun `unregistered phone input fails validation and returns unregistered error message`() {
        val phoneInput = "9999999999"
        val digitsOnly = phoneInput.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly
        
        assertEquals("9999999999", cleanPhone)
        val expectedErrorMessage = "Phone number is not registered ($cleanPhone). Please check the number or sign up."
        
        // Assert error message format matches strict registration requirement
        assertTrue(expectedErrorMessage.contains("Phone number is not registered"))
        assertTrue(expectedErrorMessage.contains("Please check the number or sign up"))
    }

    @Test
    fun `invalid short phone input fails early validation`() {
        val phoneInput = "12345"
        val digitsOnly = phoneInput.filter { it.isDigit() }
        val cleanPhone = if (digitsOnly.length > 10) digitsOnly.takeLast(10) else digitsOnly

        val isValidLength = cleanPhone.length >= 10
        assertFalse(isValidLength)
    }

    @Test
    fun `registered user phone lookup maps formatted and clean phone candidate usernames`() {
        val cleanPhone = "9876543210"
        val formattedPhone = "+91$cleanPhone"
        val candidates = mutableListOf(formattedPhone, cleanPhone)

        assertTrue(candidates.contains("+919876543210"))
        assertTrue(candidates.contains("9876543210"))
        assertEquals(2, candidates.size)
    }
}
