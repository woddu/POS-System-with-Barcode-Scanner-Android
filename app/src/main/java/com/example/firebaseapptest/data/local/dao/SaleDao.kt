package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.firebaseapptest.data.local.entity.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Upsert
    suspend fun addSale(sale: Sale)

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
}

/*
* val now = LocalDate.now()
* val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
* val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
* */