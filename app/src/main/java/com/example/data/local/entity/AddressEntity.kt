package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Address

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val houseNo: String,
    val landmark: String,
    val distanceKm: Double,
    val isSelected: Boolean
) {
    fun toDomain(): Address = Address(
        id = id,
        userId = userId,
        houseNo = houseNo,
        landmark = landmark,
        distanceKm = distanceKm,
        isSelected = isSelected
    )

    companion object {
        fun fromDomain(address: Address): AddressEntity = AddressEntity(
            id = address.id,
            userId = address.userId,
            houseNo = address.houseNo,
            landmark = address.landmark,
            distanceKm = address.distanceKm,
            isSelected = address.isSelected
        )
    }
}
