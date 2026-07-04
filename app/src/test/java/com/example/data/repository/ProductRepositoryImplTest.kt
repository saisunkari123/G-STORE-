package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.domain.model.Category
import com.example.domain.model.Product
import com.example.domain.model.ProductVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProductRepositoryImplTest {

    private class FakeCategoryDao : CategoryDao {
        val categoriesState = MutableStateFlow<List<CategoryEntity>>(emptyList())

        override fun getAllCategories(): Flow<List<CategoryEntity>> = categoriesState

        override suspend fun insertCategories(categories: List<CategoryEntity>) {
            categoriesState.update { current ->
                val map = current.associateBy { it.id }.toMutableMap()
                categories.forEach { map[it.id] = it }
                map.values.toList()
            }
        }

        override suspend fun clearCategories() {
            categoriesState.value = emptyList()
        }
    }

    private class FakeProductDao : ProductDao {
        val productsState = MutableStateFlow<List<ProductEntity>>(emptyList())

        override fun getAllProducts(): Flow<List<ProductEntity>> = productsState

        override suspend fun getProductById(productId: String): ProductEntity? {
            return productsState.value.find { it.id == productId }
        }

        override suspend fun insertProducts(products: List<ProductEntity>) {
            productsState.update { current ->
                val map = current.associateBy { it.id }.toMutableMap()
                products.forEach { map[it.id] = it }
                map.values.toList()
            }
        }

        override suspend fun insertProduct(product: ProductEntity) {
            productsState.update { current ->
                val map = current.associateBy { it.id }.toMutableMap()
                map[product.id] = product
                map.values.toList()
            }
        }

        override suspend fun deleteProduct(productId: String) {
            productsState.update { current -> current.filter { it.id != productId } }
        }

        override suspend fun clearProducts() {
            productsState.value = emptyList()
        }
    }

    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var productDao: FakeProductDao
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setup() {
        categoryDao = FakeCategoryDao()
        productDao = FakeProductDao()
        repository = ProductRepositoryImpl(categoryDao, productDao)
    }

    @Test
    fun `getAllCategories returns mapped categories`() = runTest {
        // Arrange
        val categoryEntities = listOf(
            CategoryEntity("1", "Veg", "Vegetables", "url1"),
            CategoryEntity("2", "Fruit", "Fruits", "url2")
        )
        categoryDao.categoriesState.value = categoryEntities

        // Act
        val result = repository.getAllCategories().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Veg", result[0].nameEn)
        assertEquals("2", result[1].id)
        assertEquals("Fruit", result[1].nameEn)
    }

    @Test
    fun `saveCategories inserts correctly mapped categories`() = runTest {
        // Arrange
        val categories = listOf(
            Category("1", "Veg", "Vegetables", "url1")
        )

        // Act
        repository.saveCategories(categories)

        // Assert
        val savedEntities = categoryDao.categoriesState.value
        assertEquals(1, savedEntities.size)
        assertEquals("1", savedEntities[0].id)
        assertEquals("Veg", savedEntities[0].nameEn)
    }

    @Test
    fun `clearCategories removes all categories`() = runTest {
        // Arrange
        categoryDao.categoriesState.value = listOf(CategoryEntity("1", "Veg", "Vegetables", "url1"))

        // Act
        repository.clearCategories()

        // Assert
        val savedEntities = categoryDao.categoriesState.value
        assertEquals(0, savedEntities.size)
    }

    @Test
    fun `getAllProducts returns mapped products`() = runTest {
        // Arrange
        val productEntities = listOf(
            ProductEntity("1", "c1", "Apple", "AppleTe", "Brand", "Desc", "DescTe", "Short", "ShortTe", emptyList(), 0, emptyList(), true, true, emptyList(), "SKU", 0L, 0L),
            ProductEntity("2", "c2", "Banana", "BananaTe", "Brand", "Desc", "DescTe", "Short", "ShortTe", emptyList(), 0, emptyList(), true, true, emptyList(), "SKU", 0L, 0L)
        )
        productDao.productsState.value = productEntities

        // Act
        val result = repository.getAllProducts().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Apple", result[0].nameEn)
        assertEquals("2", result[1].id)
        assertEquals("Banana", result[1].nameEn)
    }

    @Test
    fun `getProductById returns mapped product when exists`() = runTest {
        // Arrange
        val productEntity = ProductEntity("1", "c1", "Apple", "AppleTe", "Brand", "Desc", "DescTe", "Short", "ShortTe", emptyList(), 0, emptyList(), true, true, emptyList(), "SKU", 0L, 0L)
        productDao.productsState.value = listOf(productEntity)

        // Act
        val result = repository.getProductById("1")

        // Assert
        assertEquals("1", result?.id)
        assertEquals("Apple", result?.nameEn)
    }

    @Test
    fun `getProductById returns null when does not exist`() = runTest {
        // Act
        val result = repository.getProductById("999")

        // Assert
        assertNull(result)
    }

    @Test
    fun `saveProducts inserts correctly mapped products`() = runTest {
        // Arrange
        val products = listOf(
            Product("1", "c1", "Apple", "AppleTe", "Brand", "Desc", "DescTe", "Short", "ShortTe", emptyList(), 0, emptyList(), true, true, emptyList(), "SKU", 0L, 0L)
        )

        // Act
        repository.saveProducts(products)

        // Assert
        val savedEntities = productDao.productsState.value
        assertEquals(1, savedEntities.size)
        assertEquals("1", savedEntities[0].id)
        assertEquals("Apple", savedEntities[0].nameEn)
    }

    @Test
    fun `saveProduct inserts correctly mapped product`() = runTest {
        // Arrange
        val product = Product("1", "c1", "Apple", "AppleTe", "Brand", "Desc", "DescTe", "Short", "ShortTe", emptyList(), 0, emptyList(), true, true, emptyList(), "SKU", 0L, 0L)

        // Act
        repository.saveProduct(product)

        // Assert
        val savedEntities = productDao.productsState.value
        assertEquals(1, savedEntities.size)
        assertEquals("1", savedEntities[0].id)
        assertEquals("Apple", savedEntities[0].nameEn)
    }

    @Test
    fun `deleteProduct removes product`() = runTest {
        // Arrange
        val productEntity = ProductEntity("1", "c1", "Apple", "AppleTe", "Brand", "Desc", "DescTe", "Short", "ShortTe", emptyList(), 0, emptyList(), true, true, emptyList(), "SKU", 0L, 0L)
        productDao.productsState.value = listOf(productEntity)

        // Act
        repository.deleteProduct("1")

        // Assert
        val savedEntities = productDao.productsState.value
        assertEquals(0, savedEntities.size)
    }
}
