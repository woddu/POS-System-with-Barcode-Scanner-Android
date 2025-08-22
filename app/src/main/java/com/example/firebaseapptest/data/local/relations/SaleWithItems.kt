package com.example.firebaseapptest.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",             // from Sale
        entityColumn = "code",           // from Item
        associateBy = Junction(          // the join table
            value = SaleItem::class,
            parentColumn = "sale_id",    // in SaleItem
            entityColumn = "item_code"   // in SaleItem
        )
    )
    val items: List<Item>
)
