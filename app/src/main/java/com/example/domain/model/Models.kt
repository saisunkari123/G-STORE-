package com.example.domain.model

import com.google.firebase.firestore.PropertyName

data class User(
    val id: String = "",
    val phone: String = "",
    val name: String = "",
    val role: String = "CUSTOMER", // "CUSTOMER", "ADMIN"
    val email: String = "",
    val pinOrPassword: String = ""
)

data class Category(
    val id: String = "",
    val nameEn: String = "",
    val description: String = "",
    val imageUrl: String = ""
)

data class ProductVariant(
    val id: String = "",
    val weight: String = "",
    val unit: String = "Kg",
    val currentPrice: Double = 0.0,
    val mrp: Double = 0.0,
    val stockQuantity: Int = 0,
    val sku: String = ""
)

data class Product(
    val id: String = "",
    val categoryId: String = "",
    val nameEn: String = "",
    val nameTe: String = "",
    val brand: String = "",
    val descriptionEn: String = "",
    val descriptionTe: String = "",
    val shortDescriptionEn: String = "",
    val shortDescriptionTe: String = "",
    val imageUrls: List<String> = emptyList(),
    val thumbnailIndex: Int = 0,
    val variants: List<ProductVariant> = emptyList(),
    @get:PropertyName("available") @set:PropertyName("available") var isAvailable: Boolean = true,
    @get:PropertyName("enabled") @set:PropertyName("enabled") var isEnabled: Boolean = true,
    val tags: List<String> = emptyList(),
    val sku: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class Address(
    val id: String = "",
    val userId: String = "",
    val houseNo: String = "",
    val landmark: String = "",
    val distanceKm: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @get:PropertyName("selected") @set:PropertyName("selected") var isSelected: Boolean = false
)

enum class OrderStatus {
    PENDING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val addressHouseNo: String = "",
    val addressLandmark: String = "",
    val distanceKm: Double = 0.0,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val selectedSize: String = "",
    val priceAtPurchase: Double = 0.0,
    val quantity: Int = 1
)
