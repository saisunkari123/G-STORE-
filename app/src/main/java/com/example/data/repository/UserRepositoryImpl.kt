package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    override fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { it?.toDomain() }
    }

    override suspend fun getUserByEmailAndRole(email: String, role: String): User? {
        return userDao.getUserByEmailAndRole(email, role)?.toDomain()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(UserEntity.fromDomain(user))
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { list -> list.map { it.toDomain() } }
    }
}
