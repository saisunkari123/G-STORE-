package com.example.data.local.db

import androidx.room.TypeConverter
import com.example.domain.model.ProductVariant
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromProductVariantList(value: List<ProductVariant>?): String {
        val listType = Types.newParameterizedType(List::class.java, ProductVariant::class.java)
        val adapter = moshi.adapter<List<ProductVariant>>(listType)
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toProductVariantList(value: String?): List<ProductVariant> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = Types.newParameterizedType(List::class.java, ProductVariant::class.java)
        val adapter = moshi.adapter<List<ProductVariant>>(listType)
        return adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(listType)
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(listType)
        return adapter.fromJson(value) ?: emptyList()
    }
}
