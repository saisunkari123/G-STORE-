package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderCalculationTest {

    @Test
    fun `order subtotal calculates correctly from items`() {
        val item1Price = 1450.0 // 26kg Sona Masoori
        val item1Qty = 2
        val item2Price = 750.0  // 10kg HMT Rice
        val item2Qty = 1

        val subtotal = (item1Price * item1Qty) + (item2Price * item2Qty)
        assertEquals(3650.0, subtotal, 0.01)
    }

    @Test
    fun `order total includes delivery fee when subtotal is under threshold`() {
        val subtotal = 800.0
        val freeDeliveryThreshold = 1000.0
        val standardDeliveryFee = 50.0

        val deliveryFee = if (subtotal >= freeDeliveryThreshold) 0.0 else standardDeliveryFee
        val total = subtotal + deliveryFee

        assertEquals(50.0, deliveryFee, 0.01)
        assertEquals(850.0, total, 0.01)
    }

    @Test
    fun `order total waives delivery fee when subtotal meets free threshold`() {
        val subtotal = 1500.0
        val freeDeliveryThreshold = 1000.0
        val standardDeliveryFee = 50.0

        val deliveryFee = if (subtotal >= freeDeliveryThreshold) 0.0 else standardDeliveryFee
        val total = subtotal + deliveryFee

        assertEquals(0.0, deliveryFee, 0.01)
        assertEquals(1500.0, total, 0.01)
    }
}
