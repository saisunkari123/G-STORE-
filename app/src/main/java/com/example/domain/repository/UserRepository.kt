package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(userId: String): Flow<User?>
    suspend fun getUserByEmailAndRole(email: String, role: String): User?
    suspend fun saveUser(user: User)
    suspend fun deleteUser(userId: String)
    fun getAllUsers(): Flow<List<User>>
}
