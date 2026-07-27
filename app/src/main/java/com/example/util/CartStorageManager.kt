package com.example.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object CartStorageManager {

    private const val PREFS_NAME = "gstore_cart_prefs"
    private const val KEY_CART_ITEMS_PREFIX = "key_cart_items_"
    private const val KEY_SELECTED_ADDRESS_PREFIX = "key_selected_address_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getCartKey(userId: String?): String {
        val safeUser = if (userId.isNullOrBlank()) "guest" else userId.trim()
        return "$KEY_CART_ITEMS_PREFIX$safeUser"
    }

    private fun getAddressKey(userId: String?): String {
        val safeUser = if (userId.isNullOrBlank()) "guest" else userId.trim()
        return "$KEY_SELECTED_ADDRESS_PREFIX$safeUser"
    }

    /**
     * Saves cart items map (product_variant_key -> quantity) for a specific user.
     */
    fun saveCartItems(context: Context, userId: String?, cartItems: Map<String, Int>) {
        val json = JSONObject()
        cartItems.forEach { (key, qty) ->
            json.put(key, qty)
        }
        val targetKey = getCartKey(userId)
        getPrefs(context).edit().putString(targetKey, json.toString()).apply()
    }

    /**
     * Loads cart items map for a specific user from SharedPreferences.
     */
    fun loadCartItems(context: Context, userId: String? = null): Map<String, Int> {
        val targetKey = getCartKey(userId)
        val jsonStr = getPrefs(context).getString(targetKey, null) ?: return emptyMap()
        val resultMap = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val qty = json.getInt(key)
                if (qty > 0) {
                    resultMap[key] = qty
                }
            }
        } catch (_: Exception) {}
        return resultMap
    }

    /**
     * Saves selected delivery address ID for a specific user.
     */
    fun saveSelectedAddressId(context: Context, userId: String?, addressId: String) {
        val targetKey = getAddressKey(userId)
        getPrefs(context).edit().putString(targetKey, addressId).apply()
    }

    /**
     * Loads selected delivery address ID for a specific user.
     */
    fun loadSelectedAddressId(context: Context, userId: String? = null): String? {
        val targetKey = getAddressKey(userId)
        return getPrefs(context).getString(targetKey, null)
    }

    /**
     * Clears cart items for a specific user from SharedPreferences.
     */
    fun clearCart(context: Context, userId: String? = null) {
        val targetKey = getCartKey(userId)
        getPrefs(context).edit().remove(targetKey).apply()
    }
}
