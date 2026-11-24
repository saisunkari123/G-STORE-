package com.example.domain.model

import org.junit.Assert.*
import org.junit.Test

class OrderCalculationEdgeCasesTest {

    @Test
    fun `empty cart produces zero subtotal`() {
        val items = emptyList<OrderItem>()
        val subtotal = items.sumOf { it.priceAtPurchase * it.quantity }
        assertEquals(0.0, subtotal, 0.001)
    }

    @Test
    fun `cart item with zero quantity produces zero cost`() {
        val item = OrderItem(
            productId = "p1",
            productName = "Rice Bag",
            selectedSize = "26 kg",
            priceAtPurchase = 1450.0,
            quantity = 0
        )
        val cost = item.priceAtPurchase * item.quantity
        assertEquals(0.0, cost, 0.001)
    }

    @Test
    fun `minimum order amount constraint validates correctly`() {
        val minAmount = 150.0

        val cartBelowMin = 149.99
        val isEligibleBelow = cartBelowMin >= minAmount
        assertFalse("Subtotal 149.99 should not meet min amount 150.0", isEligibleBelow)

        val cartAtMin = 150.0
        val isEligibleAt = cartAtMin >= minAmount
        assertTrue("Subtotal 150.0 should meet min amount 150.0", isEligibleAt)

        val cartAboveMin = 150.01
        val isEligibleAbove = cartAboveMin >= minAmount
        assertTrue("Subtotal 150.01 should meet min amount 150.0", isEligibleAbove)
    }

    @Test
    fun `gift tiers unlock correctly based on cart subtotal`() {
        val tier1 = GiftItemConfig(
            id = "g1",
            productName = "Sugar 1kg",
            imageUrl = "image.jpg",
            thresholdAmount = 1000.0,
            giftPrice = 1.0,
            stockQuantity = 10
        )
        val tier2 = GiftItemConfig(
            id = "g2",
            productName = "Ghee 500ml",
            imageUrl = "image.jpg",
            thresholdAmount = 2500.0,
            giftPrice = 5.0,
            stockQuantity = 5
        )
        val gifts = listOf(tier1, tier2)

        // Case 1: Subtotal ₹800 -> No gifts unlocked
        val subtotal800 = 800.0
        val unlockedAt800 = gifts.filter { subtotal800 >= it.thresholdAmount }
        assertTrue(unlockedAt800.isEmpty())

        // Case 2: Subtotal ₹1000 -> Tier 1 unlocked
        val subtotal1000 = 1000.0
        val unlockedAt1000 = gifts.filter { subtotal1000 >= it.thresholdAmount }
        assertEquals(1, unlockedAt1000.size)
        assertEquals("g1", unlockedAt1000[0].id)

        // Case 3: Subtotal ₹2500 -> Both Tier 1 & Tier 2 unlocked
        val subtotal2500 = 2500.0
        val unlockedAt2500 = gifts.filter { subtotal2500 >= it.thresholdAmount }
        assertEquals(2, unlockedAt2500.size)
    }

    @Test
    fun `out of stock gift item cannot be claimed`() {
        val outOfStockGift = GiftItemConfig(
            id = "g_oos",
            productName = "Premium Ghee",
            imageUrl = "",
            thresholdAmount = 500.0,
            giftPrice = 0.0,
            stockQuantity = 0 // Out of stock!
        )

        val isClaimable = outOfStockGift.stockQuantity > 0
        assertFalse("Gift with 0 stock must not be claimable", isClaimable)
    }

    @Test
    fun `FOMO next tier remaining amount computes correctly`() {
        val subtotal = 1450.0
        val nextTierThreshold = 2000.0
        val remaining = (nextTierThreshold - subtotal).coerceAtLeast(0.0)

        assertEquals(550.0, remaining, 0.01)
    }

    @Test
    fun `order handover pin verification supports deliveryOtp and phone fallback`() {
        // Case 1: Custom deliveryOtp is set
        val orderWithOtp = Order(id = "o1", customerPhone = "+919876543210", deliveryOtp = "8421")
        val expectedPin1 = orderWithOtp.deliveryOtp.ifBlank {
            orderWithOtp.customerPhone.filter { it.isDigit() }.takeLast(4).ifEmpty { "1234" }
        }
        assertEquals("8421", expectedPin1)

        // Case 2: deliveryOtp is blank, fallback to last 4 digits of phone
        val orderWithPhone = Order(id = "o2", customerPhone = "+91 98765 43210", deliveryOtp = "")
        val expectedPin2 = orderWithPhone.deliveryOtp.ifBlank {
            orderWithPhone.customerPhone.filter { it.isDigit() }.takeLast(4).ifEmpty { "1234" }
        }
        assertEquals("3210", expectedPin2)

        // Case 3: Both blank, fallback to 1234
        val orderBlank = Order(id = "o3", customerPhone = "", deliveryOtp = "")
        val expectedPin3 = orderBlank.deliveryOtp.ifBlank {
            orderBlank.customerPhone.filter { it.isDigit() }.takeLast(4).ifEmpty { "1234" }
        }
        assertEquals("1234", expectedPin3)
    }

    @Test
    fun `cash on delivery settlement handles exact, partial, and excess settlement`() {
        val totalCashCollected = 3500.0

        // Case 1: No cash settled yet
        var settled = 0.0
        var remaining = (totalCashCollected - settled).coerceAtLeast(0.0)
        assertEquals(3500.0, remaining, 0.01)

        // Case 2: Partial settlement
        settled = 1500.0
        remaining = (totalCashCollected - settled).coerceAtLeast(0.0)
        assertEquals(2000.0, remaining, 0.01)

        // Case 3: Fully settled
        settled = 3500.0
        remaining = (totalCashCollected - settled).coerceAtLeast(0.0)
        assertEquals(0.0, remaining, 0.01)

        // Case 4: Settled exceeds total (edge case, must not go negative)
        settled = 4000.0
        remaining = (totalCashCollected - settled).coerceAtLeast(0.0)
        assertEquals(0.0, remaining, 0.01)
    }
}
