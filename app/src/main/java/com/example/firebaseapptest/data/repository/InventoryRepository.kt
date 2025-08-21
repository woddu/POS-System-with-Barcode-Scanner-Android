package com.example.firebaseapptest.data.repository

import com.example.firebaseapptest.data.local.dao.ItemDao
import com.example.firebaseapptest.data.local.dao.SaleDao
import com.example.firebaseapptest.data.local.dao.SaleItemDao
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao
) {
    fun getItem(code: Long) = itemDao.getItemByCode(code)
    fun getAllItems() = itemDao.getItems()
    suspend fun insertItem(item: Item) = itemDao.addItem(item)
    suspend fun deleteItem(item: Item)  = itemDao.deleteItem(item)

    fun getSales() = saleDao.getSales()
    fun getTodaySales(startOfDay: Long, endOfDay: Long) = saleDao.getTodaySales(startOfDay,endOfDay)
    suspend fun addSale(sale: Sale) = saleDao.addSale(sale)
    suspend fun deleteSale(sale: Sale) = saleDao.deleteSale(sale)

    fun getSaleItems() = saleItemDao.getAllSaleItems()
    fun getSaleItemById(id: Int) = saleItemDao.getSaleItemById(id)
    fun getSaleItemsBySaleId(id: Int) = saleItemDao.getSaleItemsBySaleId(id)
    fun insertSaleItem(saleItem: SaleItem) = saleItemDao.insertSaleItem(saleItem)
    fun insertSaleItems(saleItems: List<SaleItem>) = saleItemDao.insertSaleItems(saleItems)
    fun updateSaleItem(saleItem: SaleItem) = saleItemDao.updateSaleItem(saleItem)
    fun updateSaleItems(saleItems: List<SaleItem>) = saleItemDao.updateSaleItems(saleItems)
    fun deleteSaleItem(saleItem: SaleItem) = saleItemDao.deleteSaleItem(saleItem)
    fun deleteSaleItemById(id: Int) = saleItemDao.deleteSaleItemById(id)
    fun deleteBySaleId(saleId: Int) = saleItemDao.deleteBySaleId(saleId)
}
