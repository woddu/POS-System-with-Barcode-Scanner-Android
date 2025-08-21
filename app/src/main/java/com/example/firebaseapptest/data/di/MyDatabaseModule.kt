package com.example.firebaseapptest.data.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.firebaseapptest.data.local.InventoryDatabase
import com.example.firebaseapptest.data.local.dao.ItemDao
import com.example.firebaseapptest.data.local.dao.SaleDao
import com.example.firebaseapptest.data.local.dao.SaleItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InventoryDatabase {
        return Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database")
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideItemDao(db: InventoryDatabase): ItemDao = db.itemDao()

    @Provides
    fun provideSaleDao(db: InventoryDatabase): SaleDao = db.saleDao()

    @Provides
    fun provideSaleItemDao(db: InventoryDatabase): SaleItemDao = db.saleItemDao()
}
