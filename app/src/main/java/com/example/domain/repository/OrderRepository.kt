package com.example.domain.repository

import com.example.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getAllOrders(): Flow<List<Order>>
    fun getOrdersByUserId(userId: String): Flow<List<Order>>
    suspend fun saveOrder(order: Order)
    suspend fun updateOrderStatus(orderId: String, newStatus: String)
}
