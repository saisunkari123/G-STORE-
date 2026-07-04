package com.example.domain.repository

import com.example.domain.model.Address
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun getAddressesByUserId(userId: String): Flow<List<Address>>
    suspend fun saveAddress(address: Address)
    suspend fun saveAndSelectAddress(address: Address)
    suspend fun selectAddress(userId: String, addressId: String)
    suspend fun deleteAddress(addressId: String)
}
