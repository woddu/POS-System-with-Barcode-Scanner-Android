package com.example.firebaseapptest.data.local.entity.helpermodels

import com.example.firebaseapptest.data.local.entity.Sale


data class SaleWithItemNames(
    val sale: Sale,
    val items: List<SaleItemNameOnly>
)
