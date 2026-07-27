package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.Address
import org.json.JSONObject

object CartStorageManager {

    private const val PREFS_NAME = "gstore_cart_prefs"
    private const val KEY_CART_ITEMS = "key_cart_items_json"
    private const val KEY_SELECTED_ADDRESS_ID = "key_selected_address_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves cart items map (product_variant_key -> quantity) to SharedPreferences.
     */
    fun saveCartItems(context: Context, cartItems: Map<String, Int>) {
        val json = JSONObject()
        cartItems.forEach { (key, qty) ->
            json.put(key, qty)
        }
        getPrefs(context).edit().putString(KEY_CART_ITEMS, json.toString()).apply()
    }

    /**
     * Loads cart items map from SharedPreferences.
     */
    fun loadCartItems(context: Context): Map<String, Int> {
        val jsonStr = getPrefs(context).getString(KEY_CART_ITEMS, null) ?: return emptyMap()
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
     * Saves selected delivery address ID.
     */
    fun saveSelectedAddressId(context: Context, addressId: String) {
        getPrefs(context).edit().putString(KEY_SELECTED_ADDRESS_ID, addressId).apply()
    }

    /**
     * Loads selected delivery address ID.
     */
    fun loadSelectedAddressId(context: Context): String? {
        return getPrefs(context).getString(KEY_SELECTED_ADDRESS_ID, null)
    }

    /**
     * Clears cart items from SharedPreferences.
     */
    fun clearCart(context: Context) {
        getPrefs(context).edit().remove(KEY_CART_ITEMS).apply()
    }
}
