package com.example.util

import org.junit.Assert.*
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun testHashPassword_knownSha256() {
        val raw = "Ram@1234"
        val hash = SecurityUtils.hashPassword(raw)
        assertEquals("63c6f625eb63641e7987cd2abc94fe13da5e3b52b5248a7dc1b289d92832d149", hash)
    }

    @Test
    fun testVerifyPassword_success() {
        val raw = "Ram@1234"
        val expectedHash = "63c6f625eb63641e7987cd2abc94fe13da5e3b52b5248a7dc1b289d92832d149"
        assertTrue(SecurityUtils.verifyPassword(raw, expectedHash))
    }

    @Test
    fun testVerifyPassword_failure() {
        val wrong = "Wrong@1234"
        val expectedHash = "63c6f625eb63641e7987cd2abc94fe13da5e3b52b5248a7dc1b289d92832d149"
        assertFalse(SecurityUtils.verifyPassword(wrong, expectedHash))
    }

    @Test
    fun testHashPassword_blankInput() {
        assertEquals("", SecurityUtils.hashPassword(""))
        assertEquals("", SecurityUtils.hashPassword("   "))
        assertFalse(SecurityUtils.verifyPassword("", "somehash"))
    }
}
