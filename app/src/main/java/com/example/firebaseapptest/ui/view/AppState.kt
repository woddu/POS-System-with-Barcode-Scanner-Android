package com.example.firebaseapptest.ui.view

import com.example.firebaseapptest.data.local.entity.ItemForSale
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem

data class AppState(
    val sales : List<Sale> = emptyList(),

    val itemsInCounter: List<ItemForSale> = emptyList(),

    val navigateToScanner: Boolean = false,
    val navigateBackTo: String = "",

    val scannedCode: String = "",
    val scannedName: String = "",
    val scannedPrice: String = "",

    )
