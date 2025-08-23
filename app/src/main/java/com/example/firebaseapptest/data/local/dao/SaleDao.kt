package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.relations.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert
    suspend fun addSale(sale: Sale): Long

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getSales(): Flow<List<Sale>>

    @Query("""
        SELECT * FROM sales
        WHERE date BETWEEN :startOfDay AND :endOfDay
        ORDER BY date DESC
    """)
    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSale(id: Int): Sale

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleWithItems(id: Int): SaleWithItems
}

/*
* val now = LocalDate.now()
* val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
* val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
* */