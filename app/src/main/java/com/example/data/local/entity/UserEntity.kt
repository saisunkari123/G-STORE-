package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.User
import com.example.util.SecurityUtils

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
        fun fromDomain(user: User): UserEntity {
            // Ensure passwords/PINs are stored as SHA-256 digests, never in plaintext
            val securedPinOrPassword = if (user.pinOrPassword.isNotBlank()) {
                if (user.pinOrPassword.length == 64 && user.pinOrPassword.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                    user.pinOrPassword
                } else {
                    SecurityUtils.hashPassword(user.pinOrPassword)
                }
            } else ""

            return UserEntity(
                id = user.id,
                phone = user.phone,
                name = user.name,
                role = user.role,
                email = user.email,
                pinOrPassword = securedPinOrPassword,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}
