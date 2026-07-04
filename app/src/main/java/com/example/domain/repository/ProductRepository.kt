package com.example.domain.repository

import com.example.domain.model.Category
import com.example.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun saveCategories(categories: List<Category>)
    suspend fun clearCategories()

    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(productId: String): Product?
    suspend fun saveProducts(products: List<Product>)
    suspend fun saveProduct(product: Product)
    suspend fun deleteProduct(productId: String)
}
