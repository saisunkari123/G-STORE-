package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Order

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val customerName: String,
    val customerPhone: String,
    val addressHouseNo: String,
    val addressLandmark: String,
    val distanceKm: Double,
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String,
    val createdAt: Long
) {
    companion object {
        fun fromDomain(order: Order): OrderEntity = OrderEntity(
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
            status = order.status.name,
            createdAt = order.createdAt
        )
    }
}
