package com.example.data.repository

import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.domain.model.Order
import com.example.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(private val orderDao: OrderDao) : OrderRepository {
    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getOrdersByUserId(userId: String): Flow<List<Order>> {
        return orderDao.getOrdersByUserId(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveOrder(order: Order) {
        val orderEntity = OrderEntity.fromDomain(order)
        val itemEntities = order.items.map { OrderItemEntity.fromDomain(order.id, it) }
        orderDao.insertOrder(orderEntity, itemEntities)
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }
}
