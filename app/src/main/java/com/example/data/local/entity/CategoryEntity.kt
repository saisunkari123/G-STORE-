package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val description: String,
    val imageUrl: String
) {
    fun toDomain(): Category = Category(
        id = id,
        nameEn = nameEn,
        description = description,
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(category: Category): CategoryEntity = CategoryEntity(
            id = category.id,
            nameEn = category.nameEn,
            description = category.description,
            imageUrl = category.imageUrl
        )
    }
}
