package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.firebaseapptest.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Upsert
    suspend fun addItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT code, name, price FROM items ORDER BY name")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE code = :code")
    fun getItemByCode(code: Long): Item

    @Query("SELECT code, name, price FROM items WHERE code = :code")
    fun getItemByCodeForSale(code: Long): Item
}