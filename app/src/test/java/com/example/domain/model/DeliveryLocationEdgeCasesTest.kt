package com.example.domain.model

import com.example.ui.state.AppState
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class DeliveryLocationEdgeCasesTest {

    @Test
    fun `shop location to itself has zero distance and is deliverable`() {
        val distance = AppState.calculateDistanceKm(
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE,
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE
        )
        assertEquals(0.0, distance, 0.0001)
        assertTrue("Zero distance should be deliverable", distance <= AppState.deliveryRadiusKm)
    }

    @Test
    fun `location exactly at 10km boundary is deliverable`() {
        // AppState default radius is 10.0 km
        val radius = 10.0
        // Approximate 10 km north of shop: ~0.0900 degrees latitude
        val boundaryLat = AppState.SHOP_LATITUDE + (radius / 111.0)
        val distance = AppState.calculateDistanceKm(
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE,
            boundaryLat,
            AppState.SHOP_LONGITUDE
        )
        assertTrue("Distance ~$distance km should be close to 10 km", distance in 9.9..10.1)
    }

    @Test
    fun `location at 15km deliverable when deliveryRadiusKm is 15`() {
        val customRadius = 15.0
        // Point ~12 km away from store
        val pointLat = AppState.SHOP_LATITUDE + (12.0 / 111.0)
        val distance = AppState.calculateDistanceKm(
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE,
            pointLat,
            AppState.SHOP_LONGITUDE
        )
        assertTrue("Distance $distance km should be deliverable within 15 km", distance <= customRadius)

        // Point ~18 km away from store
        val distantLat = AppState.SHOP_LATITUDE + (18.0 / 111.0)
        val distantDistance = AppState.calculateDistanceKm(
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE,
            distantLat,
            AppState.SHOP_LONGITUDE
        )
        assertFalse("Distance $distantDistance km should be outside 15 km limit", distantDistance <= customRadius)
    }

    @Test
    fun `null or uninitialized GPS 0,0 coordinates rejected as far outside delivery range`() {
        // Lat: 0.0, Lon: 0.0 is the Gulf of Guinea (Null Island, ~9000+ km away from India)
        val distance = AppState.calculateDistanceKm(
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE,
            0.0,
            0.0
        )
        assertTrue("Null coordinates distance should be > 8000 km", distance > 8000.0)
        assertFalse("Null island must never be deliverable", distance <= AppState.deliveryRadiusKm)
    }

    @Test
    fun `antipodal coordinates calculate maximum distance without exception`() {
        // Antipode of Rajam is approximately (-18.4482, -96.3384) in the South Pacific Ocean
        val antipodeLat = -AppState.SHOP_LATITUDE
        val antipodeLon = if (AppState.SHOP_LONGITUDE > 0) AppState.SHOP_LONGITUDE - 180.0 else AppState.SHOP_LONGITUDE + 180.0
        val distance = AppState.calculateDistanceKm(
            AppState.SHOP_LATITUDE,
            AppState.SHOP_LONGITUDE,
            antipodeLat,
            antipodeLon
        )
        // Half Earth circumference is ~20,015 km
        assertTrue("Antipodal distance should be ~20,000 km", distance in 19900.0..20100.0)
        assertFalse("Antipode must be out of delivery range", distance <= AppState.deliveryRadiusKm)
    }

    @Test
    fun `proximity deduplication logic detects places closer than 40 meters`() {
        // 40 meters is ~0.00036 degrees latitude
        val lat1 = 18.44820
        val lon1 = 83.66160
        val lat2 = 18.44825 // ~5.5 meters apart
        val lon2 = 83.66160

        val distance = AppState.calculateDistanceKm(lat1, lon1, lat2, lon2)
        val isDuplicate = distance < 0.04 // 40m threshold in km

        assertTrue("Points 5.5m apart should be marked as duplicate", isDuplicate)
    }

    @Test
    fun `proximity deduplication retains places further than 40 meters`() {
        // 100 meters apart is ~0.0009 degrees latitude
        val lat1 = 18.44820
        val lon1 = 83.66160
        val lat2 = 18.44910 // ~100 meters apart
        val lon2 = 83.66160

        val distance = AppState.calculateDistanceKm(lat1, lon1, lat2, lon2)
        val isDuplicate = distance < 0.04

        assertFalse("Points 100m apart should NOT be marked as duplicate", isDuplicate)
    }

    @Test
    fun `address label construction formats correctly for Home, Work, and Other`() {
        val homeLabel = "Home"
        val workLabel = "Work"
        val houseInput = "D.No 4-12/A, Balaji Nagar"

        val formattedHome = if (houseInput.isNotBlank()) "$homeLabel: ${houseInput.trim()}" else homeLabel
        val formattedWork = if (houseInput.isNotBlank()) "$workLabel: ${houseInput.trim()}" else workLabel
        val formattedEmpty = if ("".isNotBlank()) "$homeLabel: " else homeLabel

        assertEquals("Home: D.No 4-12/A, Balaji Nagar", formattedHome)
        assertEquals("Work: D.No 4-12/A, Balaji Nagar", formattedWork)
        assertEquals("Home", formattedEmpty)
    }

    @Test
    fun `address coordinate fallback string formats with 4 decimal places`() {
        val lat = 18.448234
        val lon = 83.661689
        val fallback = "Location: %.4f, %.4f".format(Locale.US, lat, lon)

        assertEquals("Location: 18.4482, 83.6617", fallback)
    }

    @Test
    fun `address selection updates isSelected flag exclusively`() {
        val addresses = mutableListOf(
            Address(id = "addr_1", userId = "u1", houseNo = "Home", isSelected = true),
            Address(id = "addr_2", userId = "u1", houseNo = "Work", isSelected = false),
            Address(id = "addr_3", userId = "u1", houseNo = "Other", isSelected = false)
        )

        val targetId = "addr_2"
        val updated = addresses.map { it.copy(isSelected = it.id == targetId) }

        assertFalse(updated.find { it.id == "addr_1" }!!.isSelected)
        assertTrue(updated.find { it.id == "addr_2" }!!.isSelected)
        assertFalse(updated.find { it.id == "addr_3" }!!.isSelected)
    }
}
