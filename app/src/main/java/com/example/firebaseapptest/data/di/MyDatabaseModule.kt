package com.example.firebaseapptest.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.firebaseapptest.data.local.InventoryDatabase
import com.example.firebaseapptest.data.local.dao.ItemDao
import com.example.firebaseapptest.data.local.dao.SaleDao
import com.example.firebaseapptest.data.local.dao.SaleItemDao
import com.example.firebaseapptest.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.MessageDigest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InventoryDatabase {
        return Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database")
            .addCallback(object: RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val bytes = MessageDigest.getInstance("SHA-256")
                        .digest("@PoSE12025".toByteArray())
                    val hashedPassword = bytes.joinToString("") { "%02x".format(it) }
                    // Insert seed data directly with SQL
                    db.execSQL("INSERT OR IGNORE INTO User (id, email, username, password, isOwner) VALUES (0, 'employee1@pos.com', 'employee1', '$hashedPassword', 0)")
                }
            })
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideItemDao(db: InventoryDatabase): ItemDao = db.itemDao()

    @Provides
    fun provideSaleDao(db: InventoryDatabase): SaleDao = db.saleDao()

    @Provides
    fun provideSaleItemDao(db: InventoryDatabase): SaleItemDao = db.saleItemDao()

    @Provides
    fun provideUserDao(db: InventoryDatabase): UserDao = db.userDao()
}
