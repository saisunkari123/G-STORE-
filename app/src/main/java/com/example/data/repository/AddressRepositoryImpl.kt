package com.example.data.repository

import com.example.data.local.dao.AddressDao
import com.example.data.local.entity.AddressEntity
import com.example.domain.model.Address
import com.example.domain.repository.AddressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddressRepositoryImpl(private val addressDao: AddressDao) : AddressRepository {
    override fun getAddressesByUserId(userId: String): Flow<List<Address>> {
        return addressDao.getAddressesByUserId(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveAddress(address: Address) {
        addressDao.insertAddress(AddressEntity.fromDomain(address))
    }

    override suspend fun saveAndSelectAddress(address: Address) {
        addressDao.insertAndSelectAddress(AddressEntity.fromDomain(address))
    }

    override suspend fun selectAddress(userId: String, addressId: String) {
        addressDao.selectAddress(userId, addressId)
    }

    override suspend fun deleteAddress(addressId: String) {
        addressDao.deleteAddress(addressId)
    }
}
