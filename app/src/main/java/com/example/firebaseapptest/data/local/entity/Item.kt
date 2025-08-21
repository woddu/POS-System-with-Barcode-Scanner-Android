package com.example.firebaseapptest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey val code: Long,
    val name: String,
    val description: String?,
    val color: String?,
    val size: String?,
    val quantity: Int? = 0,
    val sold: Int? = 0,
    val price: Double,
)