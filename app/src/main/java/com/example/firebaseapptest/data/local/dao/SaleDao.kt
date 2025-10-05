package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.helpermodels.SaleItemNameOnly
import com.example.firebaseapptest.data.local.entity.helpermodels.SaleWithItemNames
import com.example.firebaseapptest.data.local.relations.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM sales WHERE date BETWEEN :start AND :end")
    suspend fun getCountBetween(start: Long, end: Long): Int

    @Insert
    suspend fun addSale(sale: Sale): Long

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getSalesPaginated(limit: Int, offset: Int): Flow<List<Sale>>

    @Query("""
        SELECT * FROM sales
        WHERE date BETWEEN :startOfDay AND :endOfDay
        ORDER BY date DESC
    """)
    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<List<Sale>>

    @Query("""
        SELECT * FROM sales
        WHERE date BETWEEN :startOfDay AND :endOfDay
        ORDER BY date DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getSalesBetween(
        startOfDay: Long,
        endOfDay: Long,
        limit: Int,
        offset: Int
    ): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSale(id: Int): Sale

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleWithItems(id: Int): SaleWithItems

    @Query("""
        SELECT si.id AS saleItemId,
               si.sale_id AS saleId,
               si.item_code AS itemCode,
               si.quantity,
               si.price,
               i.name AS itemName
        FROM sale_items AS si
        INNER JOIN items AS i ON si.item_code = i.code
        WHERE si.sale_id = :saleId
    """)
    suspend fun getSaleItemsWithName(saleId: Int): List<SaleItemNameOnly>

    @Transaction
    suspend fun getSaleWithItemNames(saleId: Int): SaleWithItemNames {
        val sale = getSale(saleId)
        val items = getSaleItemsWithName(saleId)
        return SaleWithItemNames(sale, items)
    }

    @Query("""
        SELECT * FROM sales
        WHERE date BETWEEN :startOfDay AND :endOfDay
        ORDER BY date DESC
    """)
    fun getAllSalesBetween(
        startOfDay: Long,
        endOfDay: Long
    ): List<Sale>
    
    @Transaction
    suspend fun getSalesWithItemNamesBetween(saleId: Int, startOfDay: Long, endOfDay: Long): List<SaleWithItemNames> {
        val sales = getAllSalesBetween(startOfDay, endOfDay)
        var salesWithItemNames = mutableListOf<SaleWithItemNames>()
        for (sale in sales) {
            val items = getSaleItemsWithName(sale.id)
            salesWithItemNames.add(SaleWithItemNames(sale, items))
        }
        return salesWithItemNames
    }

}




/*
* val now = LocalDate.now()
* val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
* val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
* */