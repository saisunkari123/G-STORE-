package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.data.local.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersByUserId(userId: String): Flow<List<OrderWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderEntity(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertOrder(order: OrderEntity, items: List<OrderItemEntity>) {
        insertOrderEntity(order)
        insertOrderItems(items)
    }

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, newStatus: String)
}
