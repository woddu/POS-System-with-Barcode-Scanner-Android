package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.firebaseapptest.data.local.entity.SaleItem

@Dao
interface SaleItemDao {

    @Upsert
    suspend fun upsertSaleItems(saleItems: List<SaleItem>): List<Long>

    @Query("SELECT * FROM sale_items WHERE needSync = 1")
    suspend fun getSaleItemsToSync(): List<SaleItem>

    /*
    // 🆕 Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(saleItem: SaleItem): Long


    // 📖 Read
    @Query("SELECT * FROM sale_items WHERE id = :id LIMIT 1")
    fun getSaleItemById(id: Int): SaleItem?

    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    fun getSaleItemsBySaleId(saleId: Int): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items")
     fun getAllSaleItems(): Flow<List<SaleItem>>

    // ✏️ Update
    @Update
    suspend fun updateSaleItem(saleItem: SaleItem): Int

    @Update
    suspend fun updateSaleItems(saleItems: List<SaleItem>): Int

    // 🗑 Delete
    @Delete
    suspend fun deleteSaleItem(saleItem: SaleItem): Int

    @Query("DELETE FROM sale_items WHERE id = :id")
    suspend fun deleteSaleItemById(id: Int): Int

    @Query("DELETE FROM sale_items WHERE sale_id = :saleId")
    suspend fun deleteBySaleId(saleId: Int): Int
    */
}
