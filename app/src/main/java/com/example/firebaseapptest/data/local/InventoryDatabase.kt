package com.example.firebaseapptest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.firebaseapptest.data.local.dao.ItemDao
import com.example.firebaseapptest.data.local.dao.SaleDao
import com.example.firebaseapptest.data.local.dao.SaleItemDao
import com.example.firebaseapptest.data.local.dao.UserDao
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem
import com.example.firebaseapptest.data.local.entity.User

@Database(
    entities = [Item::class, Sale::class, SaleItem::class, User::class],
    version = 7
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun userDao(): UserDao
}