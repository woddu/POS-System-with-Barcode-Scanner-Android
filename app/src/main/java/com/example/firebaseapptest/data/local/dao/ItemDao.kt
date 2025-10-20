package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.helpermodels.ItemForSale
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Upsert
    suspend fun upsertItem(item: Item): Long

    @Query("UPDATE items SET sold = sold + :amount, quantity = quantity - :amount WHERE code = :code")
    suspend fun addSold(code: Long, amount: Int)

    @Delete
    suspend fun deleteItem(item: Item): Int

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getCount(): Int

    @Query("SELECT code, name, price, discount, isDiscountPercentage FROM items ORDER BY name")
    fun getItems(): Flow<List<ItemForSale>>

    @Query("SELECT code, name, price, discount, isDiscountPercentage FROM items ORDER BY name LIMIT :limit OFFSET :offset")
    fun getItemsPaginated(limit: Int, offset: Int): Flow<List<ItemForSale>>

    @Query("SELECT code, name, price, discount, isDiscountPercentage FROM items WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name LIMIT :limit OFFSET :offset ")
    fun getSearchQueryPaginated(searchQuery: String, limit: Int, offset: Int): Flow<List<ItemForSale>>

    @Query("SELECT * FROM items WHERE code = :code")
    suspend fun getItemByCode(code: Long): Item?

    @Query("SELECT code, name, price, discount, isDiscountPercentage FROM items WHERE code = :code")
    suspend fun getItemByCodeForSale(code: Long): ItemForSale?

    @Query("SELECT * FROM items WHERE needSync = 1")
    suspend fun getItemsToSync(): List<Item>

    @Query("SELECT COUNT(*) FROM items WHERE needSync = 1")
    suspend fun getCountOfItemsToSync(): Int

    @Query("SELECT * FROM items WHERE needToDelete = 1")
    suspend fun getItemsToDelete(): List<Item>

    @Query("SELECT COUNT(*) FROM items WHERE needToDelete = 1 AND needSync = 1")
    suspend fun getCountOfItemsToDelete(): Int

}