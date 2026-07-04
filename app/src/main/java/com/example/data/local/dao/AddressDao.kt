package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId")
    fun getAddressesByUserId(userId: String): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Transaction
    suspend fun insertAndSelectAddress(address: AddressEntity) {
        clearSelectionForUser(address.userId)
        insertAddress(address.copy(isSelected = true))
    }

    @Query("UPDATE addresses SET isSelected = (id = :addressId) WHERE userId = :userId")
    suspend fun selectAddress(userId: String, addressId: String)

    @Query("UPDATE addresses SET isSelected = 0 WHERE userId = :userId")
    suspend fun clearSelectionForUser(userId: String)

    @Query("DELETE FROM addresses WHERE id = :addressId")
    suspend fun deleteAddress(addressId: String)
}
