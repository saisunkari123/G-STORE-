package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.domain.model.OrderItem

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val selectedSize: String,
    val priceAtPurchase: Double,
    val quantity: Int
) {
    fun toDomain(): OrderItem = OrderItem(
        productId = productId,
        productName = productName,
        selectedSize = selectedSize,
        priceAtPurchase = priceAtPurchase,
        quantity = quantity
    )

    companion object {
        fun fromDomain(orderId: String, item: OrderItem): OrderItemEntity = OrderItemEntity(
            id = "${orderId}_${item.productId}_${item.selectedSize}",
            orderId = orderId,
            productId = item.productId,
            productName = item.productName,
            selectedSize = item.selectedSize,
            priceAtPurchase = item.priceAtPurchase,
            quantity = item.quantity
        )
    }
}
