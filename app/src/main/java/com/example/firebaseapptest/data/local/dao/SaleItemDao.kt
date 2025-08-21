package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.firebaseapptest.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {

    // 🆕 Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertSaleItem(saleItem: SaleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertSaleItems(saleItems: List<SaleItem>): List<Long>

    // 📖 Read
    @Query("SELECT * FROM sale_items WHERE id = :id LIMIT 1")
     fun getSaleItemById(id: Int): SaleItem?

    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
     fun getSaleItemsBySaleId(saleId: Int): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items")
     fun getAllSaleItems(): Flow<List<SaleItem>>

    // ✏️ Update
    @Update
     fun updateSaleItem(saleItem: SaleItem): Int

    @Update
     fun updateSaleItems(saleItems: List<SaleItem>): Int

    // 🗑 Delete
    @Delete
     fun deleteSaleItem(saleItem: SaleItem): Int

    @Query("DELETE FROM sale_items WHERE id = :id")
     fun deleteSaleItemById(id: Int): Int

    @Query("DELETE FROM sale_items WHERE sale_id = :saleId")
     fun deleteBySaleId(saleId: Int): Int
}
