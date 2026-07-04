package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.domain.model.Category
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { CategoryEntity.fromDomain(it) })
    }

    override suspend fun clearCategories() {
        categoryDao.clearCategories()
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(productId: String): Product? {
        return productDao.getProductById(productId)?.toDomain()
    }

    override suspend fun saveProducts(products: List<Product>) {
        productDao.insertProducts(products.map { ProductEntity.fromDomain(it) })
    }

    override suspend fun saveProduct(product: Product) {
        productDao.insertProduct(ProductEntity.fromDomain(product))
    }

    override suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }
}
