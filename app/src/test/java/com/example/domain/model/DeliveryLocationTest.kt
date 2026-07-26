package com.example.domain.model

import com.example.ui.state.AppState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.*

class DeliveryLocationTest {

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    @Test
    fun `location within 10km delivery radius is valid`() {
        // Shop Location: Rajam (18.4482, 83.6616)
        // Customer Location: 2km away (18.4550, 83.6700)
        val distance = calculateDistanceKm(AppState.SHOP_LATITUDE, AppState.SHOP_LONGITUDE, 18.4550, 83.6700)
        assertTrue("Distance $distance km should be within max delivery radius", distance <= AppState.MAX_DELIVERY_DISTANCE_KM)
    }

    @Test
    fun `location beyond 10km delivery radius is rejected`() {
        // Customer Location: Vizianagaram city (~40km away)
        val distance = calculateDistanceKm(AppState.SHOP_LATITUDE, AppState.SHOP_LONGITUDE, 18.1124, 83.3976)
        assertFalse("Distance $distance km should exceed max delivery radius", distance <= AppState.MAX_DELIVERY_DISTANCE_KM)
    }
}
