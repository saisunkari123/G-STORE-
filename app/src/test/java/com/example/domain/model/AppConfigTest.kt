package com.example.domain.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppConfigTest {

    private val gson = Gson()

    @Test
    fun `default AppConfig values are correct`() {
        val config = AppConfig()
        assertEquals(150.0, config.minimumOrderAmount, 0.001)
        assertEquals(10.0, config.deliveryRadiusKm, 0.001)
    }

    @Test
    fun `AppConfig serializes and deserializes correctly via Gson`() {
        val original = AppConfig(minimumOrderAmount = 250.0, deliveryRadiusKm = 15.0)
        val json = gson.toJson(original)
        
        val restored = gson.fromJson(json, AppConfig::class.java)
        assertNotNull(restored)
        assertEquals(250.0, restored.minimumOrderAmount, 0.001)
        assertEquals(15.0, restored.deliveryRadiusKm, 0.001)
    }

    @Test
    fun `sys_config json payload parsing handles custom values`() {
        val rawJson = """{"minimumOrderAmount":300.0,"deliveryRadiusKm":20.0}"""
        val config = gson.fromJson(rawJson, AppConfig::class.java)
        
        assertEquals(300.0, config.minimumOrderAmount, 0.001)
        assertEquals(20.0, config.deliveryRadiusKm, 0.001)
    }
}
