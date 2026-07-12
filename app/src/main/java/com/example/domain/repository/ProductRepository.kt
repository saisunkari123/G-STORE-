package com.example.domain.repository

import com.example.domain.model.Category
import com.example.domain.model.Product
import com.example.domain.model.GiftItemConfig
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun saveCategories(categories: List<Category>)
    suspend fun clearCategories()

    fun getGiftConfigs(): Flow<List<GiftItemConfig>>
    suspend fun saveGiftConfigs(configs: List<GiftItemConfig>)
    suspend fun clearGiftConfigs()

    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(productId: String): Product?
    suspend fun saveProducts(products: List<Product>)
    suspend fun saveProduct(product: Product)
    suspend fun deleteProduct(productId: String)

    /** Pull the latest product list from the remote cloud and update the local Flow. */
    suspend fun forceRefreshFromCloud() { /* default no-op for implementations that don't have cloud */ }
}
