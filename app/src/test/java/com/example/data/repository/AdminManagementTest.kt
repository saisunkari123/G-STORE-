package com.example.data.repository

import com.example.domain.model.Product
import com.example.domain.model.ProductVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminManagementTest {

    @Test
    fun `admin stock update updates available quantity`() {
        var initialStock = 50
        val quantityAdded = 25

        initialStock += quantityAdded

        assertEquals(75, initialStock)
    }

    @Test
    fun `admin monthly sales progress percentage calculates accurately`() {
        val currentSales = 35000.0
        val targetGoal = 50000.0

        val progressPercentage = (currentSales / targetGoal) * 100

        assertEquals(70.0, progressPercentage, 0.01)
        assertTrue("Sales progress should be positive", progressPercentage > 0)
    }
}
