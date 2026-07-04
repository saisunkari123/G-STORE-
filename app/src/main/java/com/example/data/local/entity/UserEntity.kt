package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phone: String,
    val name: String,
    val role: String,
    val email: String,
    val pinOrPassword: String,
    val createdAt: Long
) {
    fun toDomain(): User = User(
        id = id,
        phone = phone,
        name = name,
        role = role,
        email = email,
        pinOrPassword = pinOrPassword
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            phone = user.phone,
            name = user.name,
            role = user.role,
            email = user.email,
            pinOrPassword = user.pinOrPassword,
            createdAt = System.currentTimeMillis()
        )
    }
}
