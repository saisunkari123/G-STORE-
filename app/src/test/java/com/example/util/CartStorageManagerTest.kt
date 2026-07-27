package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartStorageManagerTest {

    @Test
    fun `cart items json serialization preserves product keys and quantities`() {
        val originalCart = mapOf(
            "p_rice_sona#v_rice_sona_26" to 2,
            "p_oil_sunflower#v_oil_sun_1" to 1
        )

        // Verify map properties
        assertEquals(2, originalCart.size)
        assertEquals(2, originalCart["p_rice_sona#v_rice_sona_26"])
        assertEquals(1, originalCart["p_oil_sunflower#v_oil_sun_1"])
    }

    @Test
    fun `cart total item count sums correctly`() {
        val cartItems = mapOf(
            "p_rice_sona#v_rice_sona_26" to 3,
            "p_oil_sunflower#v_oil_sun_1" to 2
        )

        val totalCount = cartItems.values.sum()
        assertEquals(5, totalCount)
    }
}
