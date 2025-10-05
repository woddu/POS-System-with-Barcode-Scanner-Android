package com.example.firebaseapptest.data.repository

import com.example.firebaseapptest.data.local.dao.ItemDao
import com.example.firebaseapptest.data.local.dao.SaleDao
import com.example.firebaseapptest.data.local.dao.SaleItemDao
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao
) {
    suspend fun getItemByCodeForSale(code: Long) = itemDao.getItemByCodeForSale(code)
    suspend fun getItem(code: Long) = itemDao.getItemByCode(code)
    suspend fun getItemsCount() = itemDao.getCount()

    fun getAllItemsPaginated(limit: Int, offset: Int) = itemDao.getItemsPaginated(limit, offset)

    fun getSearchQueryPaginated(query: String, limit: Int, offset: Int) = itemDao.getSearchQueryPaginated(query, limit, offset)

    suspend fun upsertItem(item: Item) = itemDao.upsertItem(item)

    suspend fun itemSold(code: Long, amount: Int) = itemDao.addSold(code, amount)
    suspend fun deleteItem(item: Item?)  = if(item != null)itemDao.deleteItem(item) else Unit

    suspend fun getSaleCount() = saleDao.getCount()
    suspend fun getSaleCountBetween(start: Long, end: Long) = saleDao.getCountBetween(start, end)
    suspend fun getSaleCountToday(): Int {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1
        return saleDao.getCountBetween(startOfDay,endOfDay)
    }
    fun getSalesPaginated(limit: Int, offset: Int) = saleDao.getSalesPaginated(limit,offset)
    fun getSalesBetween(startOfDay: Long, endOfDay: Long, limit: Int, offset: Int) = saleDao.getSalesBetween(startOfDay, endOfDay, limit, offset)
    fun getAllSalesBetween(startOfDay: Long, endOfDay: Long) = saleDao.getAllSalesBetween(startOfDay, endOfDay)
    fun getTodaySalesPaginated(limit: Int, offset: Int): Flow<List<Sale>> {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1

        return saleDao.getSalesBetween(startOfDay, endOfDay, limit, offset)
    }
    suspend fun getSaleWithItems(saleId: Int) = saleDao.getSaleWithItemNames(saleId)
    suspend fun addSale(sale: Sale) = saleDao.addSale(sale)
    suspend fun deleteSale(sale: Sale) = saleDao.deleteSale(sale)

    suspend fun addSaleItems(saleItems: List<SaleItem>) = saleItemDao.insertSaleItems(saleItems)
}
