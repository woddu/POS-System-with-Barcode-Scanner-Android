package com.example.firebaseapptest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.firebaseapptest.data.local.util.DateConverters
import java.time.LocalDateTime

@Entity(tableName = "sales")
@TypeConverters(DateConverters::class)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDateTime,
    val total: Double,
    val paymentMethod: String,
    val imageUri: String? = null,
    val amountPaid: Double,
    val change : Double
)
