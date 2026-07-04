package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RiceMartDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: RiceMartDatabase? = null

        fun getDatabase(context: Context): RiceMartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RiceMartDatabase::class.java,
                    "rice_mart_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
