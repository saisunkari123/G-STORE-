package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FakeUserDao : UserDao {
    private val usersFlow = MutableStateFlow<List<UserEntity>>(emptyList())

    override fun getUserById(userId: String): Flow<UserEntity?> {
        return usersFlow.map { users -> users.find { it.id == userId } }
    }

    override suspend fun getUserByEmailAndRole(email: String, role: String): UserEntity? {
        return usersFlow.value.find { it.email == email && it.role == role }
    }

    override suspend fun insertUser(user: UserEntity) {
        val currentUsers = usersFlow.value.toMutableList()
        val index = currentUsers.indexOfFirst { it.id == user.id }
        if (index != -1) {
            currentUsers[index] = user
        } else {
            currentUsers.add(user)
        }
        usersFlow.value = currentUsers
    }

    override suspend fun deleteUser(userId: String) {
        val currentUsers = usersFlow.value.toMutableList()
        currentUsers.removeAll { it.id == userId }
        usersFlow.value = currentUsers
    }

    override fun getAllUsers(): Flow<List<UserEntity>> {
        return usersFlow
    }
}

class UserRepositoryImplTest {

    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setup() {
        fakeUserDao = FakeUserDao()
        userRepository = UserRepositoryImpl(fakeUserDao)
    }

    @Test
    fun `getUserById returns mapped user`() = runTest {
        val userEntity = UserEntity(
            id = "1", phone = "1234567890", name = "Test User", role = "CUSTOMER", email = "test@example.com", pinOrPassword = "123", createdAt = 0L
        )
        fakeUserDao.insertUser(userEntity)

        val result = userRepository.getUserById("1").first()

        assertEquals("1", result?.id)
        assertEquals("Test User", result?.name)
        assertEquals("CUSTOMER", result?.role)
        assertEquals("test@example.com", result?.email)
    }

    @Test
    fun `getUserById returns null when not found`() = runTest {
        val result = userRepository.getUserById("1").first()
        assertNull(result)
    }

    @Test
    fun `getUserByEmailAndRole returns mapped user`() = runTest {
        val userEntity = UserEntity(
            id = "1", phone = "1234567890", name = "Test User", role = "CUSTOMER", email = "test@example.com", pinOrPassword = "123", createdAt = 0L
        )
        fakeUserDao.insertUser(userEntity)

        val result = userRepository.getUserByEmailAndRole("test@example.com", "CUSTOMER")

        assertEquals("1", result?.id)
        assertEquals("Test User", result?.name)
        assertEquals("CUSTOMER", result?.role)
        assertEquals("test@example.com", result?.email)
    }

    @Test
    fun `saveUser inserts correct entity`() = runTest {
        val user = User(
            id = "2", phone = "0987654321", name = "New User", role = "ADMIN", email = "admin@example.com", pinOrPassword = "456"
        )

        userRepository.saveUser(user)

        val allUsers = fakeUserDao.getAllUsers().first()
        assertEquals(1, allUsers.size)
        assertEquals("2", allUsers[0].id)
        assertEquals("New User", allUsers[0].name)
    }

    @Test
    fun `deleteUser removes user`() = runTest {
        val user = User(
            id = "2", phone = "0987654321", name = "New User", role = "ADMIN", email = "admin@example.com", pinOrPassword = "456"
        )
        userRepository.saveUser(user)

        var allUsers = fakeUserDao.getAllUsers().first()
        assertEquals(1, allUsers.size)

        userRepository.deleteUser("2")

        allUsers = fakeUserDao.getAllUsers().first()
        assertEquals(0, allUsers.size)
    }

    @Test
    fun `getAllUsers returns list of mapped users`() = runTest {
        val user1 = User(id = "1", name = "User 1")
        val user2 = User(id = "2", name = "User 2")

        userRepository.saveUser(user1)
        userRepository.saveUser(user2)

        val result = userRepository.getAllUsers().first()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }
}
