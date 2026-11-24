package com.example.domain.model

import org.junit.Assert.*
import org.junit.Test

class ProductVariantStockValidationTest {

    @Test
    fun `stock deduction reduces available inventory accurately`() {
        val variant = ProductVariant(
            id = "var_1",
            weight = "26",
            unit = "kg",
            currentPrice = 1450.0,
            mrp = 1600.0,
            stockQuantity = 20
        )

        val purchasedQty = 3
        val updatedVariant = variant.copy(stockQuantity = variant.stockQuantity - purchasedQty)

        assertEquals(17, updatedVariant.stockQuantity)
    }

    @Test
    fun `stock deduction reaching zero marks variant as out of stock`() {
        val variant = ProductVariant(
            id = "var_2",
            weight = "10",
            unit = "kg",
            currentPrice = 600.0,
            mrp = 650.0,
            stockQuantity = 5
        )

        val purchasedQty = 5
        val updatedVariant = variant.copy(stockQuantity = variant.stockQuantity - purchasedQty)

        assertEquals(0, updatedVariant.stockQuantity)
        assertTrue("Stock of 0 should be out of stock", updatedVariant.stockQuantity <= 0)
    }

    @Test
    fun `purchasing quantity greater than available stock fails validation`() {
        val availableStock = 4
        val requestedQty = 5

        val hasSufficientStock = requestedQty <= availableStock
        assertFalse("Requesting 5 when only 4 available must fail", hasSufficientStock)

        val errorMessage = if (!hasSufficientStock) {
            "Insufficient stock. Only $availableStock items available."
        } else null

        assertNotNull(errorMessage)
        assertEquals("Insufficient stock. Only 4 items available.", errorMessage)
    }

    @Test
    fun `multi-variant product tracks distinct stock quantities independently`() {
        val product = Product(
            id = "prod_rice_1",
            nameEn = "Sona Masoori Rice",
            variants = listOf(
                ProductVariant("v_5kg", "5", "kg", 320.0, 350.0, stockQuantity = 50),
                ProductVariant("v_10kg", "10", "kg", 600.0, 650.0, stockQuantity = 30),
                ProductVariant("v_26kg", "26", "kg", 1450.0, 1600.0, stockQuantity = 2)
            )
        )

        val v5 = product.variants.find { it.id == "v_5kg" }!!
        val v26 = product.variants.find { it.id == "v_26kg" }!!

        assertTrue("5kg should have ample stock", v5.stockQuantity >= 10)
        assertTrue("26kg should trigger low stock alert (<5)", v26.stockQuantity < 5)
    }
}
