package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderTimelineTest {

    private fun getOrderStepIndex(status: String): Int {
        return when (status.uppercase()) {
            "PENDING" -> 0
            "CONFIRMED" -> 1
            "OUT_FOR_DELIVERY", "DISPATCHED" -> 2
            "DELIVERED" -> 3
            "CANCELLED" -> -1
            else -> 0
        }
    }

    @Test
    fun `pending order status maps to step index 0`() {
        assertEquals(0, getOrderStepIndex("PENDING"))
    }

    @Test
    fun `confirmed order status maps to step index 1`() {
        assertEquals(1, getOrderStepIndex("CONFIRMED"))
    }

    @Test
    fun `dispatched order status maps to step index 2`() {
        assertEquals(2, getOrderStepIndex("OUT_FOR_DELIVERY"))
        assertEquals(2, getOrderStepIndex("DISPATCHED"))
    }

    @Test
    fun `delivered order status maps to step index 3`() {
        assertEquals(3, getOrderStepIndex("DELIVERED"))
    }

    @Test
    fun `cancelled order status maps to index -1`() {
        assertEquals(-1, getOrderStepIndex("CANCELLED"))
    }
}
