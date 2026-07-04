package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.RiceMartDatabase
import com.example.data.repository.ProductRepositoryImpl
import com.example.domain.model.Product
import com.example.domain.model.ProductVariant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductRepositoryPerformanceTest {

    private lateinit var database: RiceMartDatabase
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RiceMartDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = ProductRepositoryImpl(
            database.categoryDao(),
            database.productDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun benchmarkSaveProducts() = runBlocking {
        // Generate a large number of products to simulate a big cart checkout
        val products = (1..100).map { i ->
            Product(
                id = "product_$i",
                nameEn = "Test Product $i",
                descriptionEn = "Description $i",
                categoryId = "category_1",
                imageUrls = emptyList(),
                variants = listOf(
                    ProductVariant(id = "var_1", weight = "1", unit = "Kg", currentPrice = 10.0, stockQuantity = 100)
                ),
                isAvailable = true,
                dateCreated = System.currentTimeMillis()
            )
        }

        // Measure sequential saveProduct (Baseline)
        val timeSequential = measureTimeMillis {
            products.forEach { repository.saveProduct(it) }
        }
        println("Time for sequential saveProduct (Baseline): ${timeSequential}ms")

        // Clean up
        products.forEach { repository.deleteProduct(it.id) }

        // Measure batched saveProducts (Optimized)
        val timeBatched = measureTimeMillis {
            repository.saveProducts(products)
        }
        println("Time for batched saveProducts (Optimized): ${timeBatched}ms")

        // Just logging to output, but we can see the performance improvement.
    }
}
