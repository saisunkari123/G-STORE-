package com.example.data.repository

import com.example.data.local.dao.AddressDao
import com.example.data.local.entity.AddressEntity
import com.example.domain.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddressRepositoryImplTest {

    private lateinit var repository: AddressRepositoryImpl
    private lateinit var fakeDao: FakeAddressDao

    @Before
    fun setup() {
        fakeDao = FakeAddressDao()
        repository = AddressRepositoryImpl(fakeDao)
    }

    @Test
    fun getAddressesByUserId_returnsMappedAddresses() = runTest {
        // Arrange
        val userId = "user1"
        val entity1 = AddressEntity(id = "1", userId = userId, houseNo = "123", landmark = "L1", distanceKm = 1.0, isSelected = false)
        val entity2 = AddressEntity(id = "2", userId = userId, houseNo = "456", landmark = "L2", distanceKm = 2.0, isSelected = true)
        fakeDao.insertAddress(entity1)
        fakeDao.insertAddress(entity2)

        // Act
        val result = repository.getAddressesByUserId(userId).first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("123", result[0].houseNo)
        assertEquals(false, result[0].isSelected)
        assertEquals("2", result[1].id)
        assertEquals("456", result[1].houseNo)
        assertEquals(true, result[1].isSelected)
    }

    @Test
    fun saveAddress_callsDaoInsert() = runTest {
        // Arrange
        val address = Address(id = "3", userId = "user2", houseNo = "789", landmark = "L3", distanceKm = 3.0, isSelected = false)

        // Act
        repository.saveAddress(address)

        // Assert
        val savedEntities = fakeDao.getAddressesByUserId("user2").first()
        assertEquals(1, savedEntities.size)
        assertEquals("3", savedEntities[0].id)
        assertEquals("789", savedEntities[0].houseNo)
    }

    @Test
    fun saveAndSelectAddress_callsDaoInsertAndSelect() = runTest {
        // Arrange
        val address = Address(id = "4", userId = "user3", houseNo = "012", landmark = "L4", distanceKm = 4.0, isSelected = false)

        // Act
        repository.saveAndSelectAddress(address)

        // Assert
        // In the real impl, AddressDao.insertAndSelectAddress clears selection and then inserts with isSelected = true
        // For FakeAddressDao, we should simulate this behavior
        val savedEntities = fakeDao.getAddressesByUserId("user3").first()
        assertEquals(1, savedEntities.size)
        assertEquals("4", savedEntities[0].id)
        assertEquals(true, savedEntities[0].isSelected)
    }

    @Test
    fun selectAddress_callsDaoSelectAddress() = runTest {
        // Arrange
        val userId = "user1"
        fakeDao.insertAddress(AddressEntity(id = "1", userId = userId, houseNo = "123", landmark = "L1", distanceKm = 1.0, isSelected = false))
        fakeDao.insertAddress(AddressEntity(id = "2", userId = userId, houseNo = "456", landmark = "L2", distanceKm = 2.0, isSelected = true))

        // Act
        repository.selectAddress(userId, "1")

        // Assert
        val entities = fakeDao.getAddressesByUserId(userId).first()
        val addr1 = entities.find { it.id == "1" }
        val addr2 = entities.find { it.id == "2" }

        assertEquals(true, addr1?.isSelected)
        assertEquals(false, addr2?.isSelected)
    }

    @Test
    fun deleteAddress_callsDaoDelete() = runTest {
        // Arrange
        val userId = "user1"
        fakeDao.insertAddress(AddressEntity(id = "1", userId = userId, houseNo = "123", landmark = "L1", distanceKm = 1.0, isSelected = false))

        // Act
        repository.deleteAddress("1")

        // Assert
        val entities = fakeDao.getAddressesByUserId(userId).first()
        assertEquals(0, entities.size)
    }
}

class FakeAddressDao : AddressDao {
    private val addresses = mutableListOf<AddressEntity>()

    override fun getAddressesByUserId(userId: String): Flow<List<AddressEntity>> {
        return flowOf(addresses.filter { it.userId == userId })
    }

    override suspend fun insertAddress(address: AddressEntity) {
        val index = addresses.indexOfFirst { it.id == address.id }
        if (index != -1) {
            addresses[index] = address
        } else {
            addresses.add(address)
        }
    }

    override suspend fun insertAndSelectAddress(address: AddressEntity) {
        clearSelectionForUser(address.userId)
        insertAddress(address.copy(isSelected = true))
    }

    override suspend fun selectAddress(userId: String, addressId: String) {
        val updatedList = addresses.map {
            if (it.userId == userId) {
                it.copy(isSelected = it.id == addressId)
            } else {
                it
            }
        }
        addresses.clear()
        addresses.addAll(updatedList)
    }

    override suspend fun clearSelectionForUser(userId: String) {
        val updatedList = addresses.map {
            if (it.userId == userId) {
                it.copy(isSelected = false)
            } else {
                it
            }
        }
        addresses.clear()
        addresses.addAll(updatedList)
    }

    override suspend fun deleteAddress(addressId: String) {
        addresses.removeIf { it.id == addressId }
    }
}
