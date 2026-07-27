package com.example.data.repository

import com.example.domain.model.ProductVariant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LowStockAlertTest {

    private fun isLowStock(variant: ProductVariant, threshold: Int = 5): Boolean {
        return variant.stockQuantity < threshold
    }

    @Test
    fun `variant with stock less than 5 triggers low stock warning`() {
        val lowStockVariant = ProductVariant("v_1", "26", "kg", 1350.0, 1500.0, stockQuantity = 3)
        assertTrue("Stock of 3 should trigger low stock warning", isLowStock(lowStockVariant))
    }

    @Test
    fun `variant with stock equal to or greater than 5 does not trigger low stock warning`() {
        val sufficientStockVariant = ProductVariant("v_2", "10", "kg", 550.0, 600.0, stockQuantity = 12)
        assertFalse("Stock of 12 should not trigger low stock warning", isLowStock(sufficientStockVariant))
    }
}
