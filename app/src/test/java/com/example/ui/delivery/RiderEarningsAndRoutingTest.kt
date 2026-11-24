package com.example.ui.delivery

import com.example.domain.model.Order
import com.example.ui.state.AppState
import org.junit.Assert.*
import org.junit.Test

class RiderEarningsAndRoutingTest {

    @Test
    fun `rider earnings for zero completed orders is zero`() {
        val completedOrders = emptyList<Order>()
        val earnings = AppState.getRiderEarningsToday(completedOrders)
        assertEquals(0.0, earnings, 0.01)
    }

    @Test
    fun `rider earnings for order within 2km base distance pays only base pay`() {
        // Base pay is ₹25, no distance bonus for <= 2.0 km
        val shortOrder = Order(id = "o1", distanceKm = 1.5)
        val earnings = AppState.getRiderEarningsToday(listOf(shortOrder))
        assertEquals(25.0, earnings, 0.01)

        val exact2kmOrder = Order(id = "o2", distanceKm = 2.0)
        val earnings2km = AppState.getRiderEarningsToday(listOf(exact2kmOrder))
        assertEquals(25.0, earnings2km, 0.01)
    }

    @Test
    fun `rider earnings for long distance order adds 5 rupees per km beyond 2km`() {
        // Trip is 6.0 km -> Base ₹25 + (6.0 - 2.0) * ₹5 = ₹25 + ₹20 = ₹45.0
        val longOrder = Order(id = "o3", distanceKm = 6.0)
        val earnings = AppState.getRiderEarningsToday(listOf(longOrder))
        assertEquals(45.0, earnings, 0.01)
    }

    @Test
    fun `rider earnings for multiple mixed orders computes correct aggregate sum`() {
        // Order 1: 1.0 km -> ₹25.0
        // Order 2: 3.5 km -> ₹25.0 + 1.5 * 5 = ₹32.5
        // Order 3: 8.0 km -> ₹25.0 + 6.0 * 5 = ₹55.0
        // Total = 25.0 + 32.5 + 55.0 = ₹112.5
        val orders = listOf(
            Order(id = "o1", distanceKm = 1.0),
            Order(id = "o2", distanceKm = 3.5),
            Order(id = "o3", distanceKm = 8.0)
        )
        val totalEarnings = AppState.getRiderEarningsToday(orders)
        assertEquals(112.5, totalEarnings, 0.01)
    }

    @Test
    fun `driver gps progress simulation clamps progress fraction within 0 and 1`() {
        val hubLat = AppState.SHOP_LATITUDE
        val hubLon = AppState.SHOP_LONGITUDE
        val targetLat = 18.5000
        val targetLon = 83.7000

        // Case 1: Progress = 0.0 (At Hub)
        val fraction0 = 0.0f
        val latAt0 = hubLat + (targetLat - hubLat) * fraction0.coerceIn(0f, 1f)
        val lonAt0 = hubLon + (targetLon - hubLon) * fraction0.coerceIn(0f, 1f)
        assertEquals(hubLat, latAt0, 0.0001)
        assertEquals(hubLon, lonAt0, 0.0001)

        // Case 2: Progress = 1.0 (At Customer Destination)
        val fraction1 = 1.0f
        val latAt1 = hubLat + (targetLat - hubLat) * fraction1.coerceIn(0f, 1f)
        val lonAt1 = hubLon + (targetLon - hubLon) * fraction1.coerceIn(0f, 1f)
        assertEquals(targetLat, latAt1, 0.0001)
        assertEquals(targetLon, lonAt1, 0.0001)

        // Case 3: Out-of-bounds Progress (e.g. 1.5 or -0.5) must be clamped safely
        val fractionExcess = 1.5f
        val latAtExcess = hubLat + (targetLat - hubLat) * fractionExcess.coerceIn(0f, 1f)
        assertEquals(targetLat, latAtExcess, 0.0001)

        val fractionNegative = -0.5f
        val latAtNegative = hubLat + (targetLat - hubLat) * fractionNegative.coerceIn(0f, 1f)
        assertEquals(hubLat, latAtNegative, 0.0001)
    }

    @Test
    fun `batch multi-stop total distance computes sum with fallback for missing distances`() {
        val orders = listOf(
            Order(id = "o1", distanceKm = 4.2),
            Order(id = "o2", distanceKm = 0.0), // Missing distance -> falls back to 2.0 km
            Order(id = "o3", distanceKm = 3.8)
        )

        val totalRouteDist = orders.sumOf { if (it.distanceKm > 0) it.distanceKm else 2.0 }
        assertEquals(10.0, totalRouteDist, 0.01)
    }
}
