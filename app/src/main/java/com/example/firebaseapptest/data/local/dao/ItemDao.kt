package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.ItemForSale
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Upsert
    suspend fun upsertItem(item: Item): Long

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getCount(): Int

    @Query("SELECT code, name, price FROM items ORDER BY name")
    fun getItems(): Flow<List<ItemForSale>>

    @Query("SELECT code, name, price FROM items ORDER BY name LIMIT :limit OFFSET :offset")
    fun getItemsPaginated(limit: Int, offset: Int): Flow<List<ItemForSale>>

    @Query("SELECT * FROM items WHERE code = :code")
    suspend fun getItemByCode(code: Long): Item?

    @Query("SELECT code, name, price FROM items WHERE code = :code")
    suspend fun getItemByCodeForSale(code: Long): ItemForSale?
}