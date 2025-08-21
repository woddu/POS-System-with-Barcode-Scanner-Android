package com.example.firebaseapptest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["sale_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
            parentColumns = ["code"],
            childColumns = ["item_code"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sale_id"), Index("item_code")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "sale_id") val saleId: Int,
    @ColumnInfo(name = "item_code") val itemCode: Long,
    val quantity: Int,
    val price: Double
)
