package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
) {
    fun toDomain(): Order = Order(
        id = order.id,
        userId = order.userId,
        customerName = order.customerName,
        customerPhone = order.customerPhone,
        addressHouseNo = order.addressHouseNo,
        addressLandmark = order.addressLandmark,
        distanceKm = order.distanceKm,
        subtotal = order.subtotal,
        deliveryFee = order.deliveryFee,
        totalAmount = order.totalAmount,
        status = OrderStatus.valueOf(order.status),
        createdAt = order.createdAt,
        items = items.map { it.toDomain() }
    )
}
