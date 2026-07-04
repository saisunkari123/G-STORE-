package com.example

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

class DeletePerformanceTest {

    private suspend fun deleteProduct(id: String) {
        // Simulate IO delay for DB operation
        delay(50)
    }

    @Test
    fun benchmarkSequentialVsConcurrent() = runBlocking {
        val legacyIds = listOf("p_sonamasoori", "p_basmati")

        // Warmup
        legacyIds.forEach { deleteProduct(it) }

        val timeSequential = measureTimeMillis {
            legacyIds.forEach { id ->
                try { deleteProduct(id) } catch (_: Exception) {}
            }
        }

        val timeConcurrent = measureTimeMillis {
            legacyIds.map { id ->
                async {
                    try { deleteProduct(id) } catch (_: Exception) {}
                }
            }.awaitAll()
        }

        println("Sequential deletion took: $timeSequential ms")
        println("Concurrent deletion took: $timeConcurrent ms")

        assert(timeConcurrent <= timeSequential + 20) // Some overhead might exist for very fast operations, but for 50ms delay, concurrent should be ~50ms, sequential ~100ms.
    }
}
