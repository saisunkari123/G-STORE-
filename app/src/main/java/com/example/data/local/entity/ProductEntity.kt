package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Product
import com.example.domain.model.ProductVariant

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val nameEn: String,
    val nameTe: String,
    val brand: String,
    val descriptionEn: String,
    val descriptionTe: String,
    val shortDescriptionEn: String,
    val shortDescriptionTe: String,
    val imageUrls: List<String>,
    val thumbnailIndex: Int,
    val variants: List<ProductVariant>,
    val isAvailable: Boolean,
    val isEnabled: Boolean,
    val tags: List<String>,
    val sku: String,
    val dateCreated: Long,
    val lastUpdated: Long
) {
    fun toDomain(): Product = Product(
        id = id,
        categoryId = categoryId,
        nameEn = nameEn,
        nameTe = nameTe,
        brand = brand,
        descriptionEn = descriptionEn,
        descriptionTe = descriptionTe,
        shortDescriptionEn = shortDescriptionEn,
        shortDescriptionTe = shortDescriptionTe,
        imageUrls = imageUrls,
        thumbnailIndex = thumbnailIndex,
        variants = variants,
        isAvailable = isAvailable,
        isEnabled = isEnabled,
        tags = tags,
        sku = sku,
        dateCreated = dateCreated,
        lastUpdated = lastUpdated
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            categoryId = product.categoryId,
            nameEn = product.nameEn,
            nameTe = product.nameTe,
            brand = product.brand,
            descriptionEn = product.descriptionEn,
            descriptionTe = product.descriptionTe,
            shortDescriptionEn = product.shortDescriptionEn,
            shortDescriptionTe = product.shortDescriptionTe,
            imageUrls = product.imageUrls,
            thumbnailIndex = product.thumbnailIndex,
            variants = product.variants,
            isAvailable = product.isAvailable,
            isEnabled = product.isEnabled,
            tags = product.tags,
            sku = product.sku,
            dateCreated = product.dateCreated,
            lastUpdated = product.lastUpdated
        )
    }
}
