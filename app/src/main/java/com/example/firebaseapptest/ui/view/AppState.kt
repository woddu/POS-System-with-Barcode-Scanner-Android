package com.example.firebaseapptest.ui.view

import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem

data class AppState(
    val items : List<Item> = emptyList(),
    val sales : List<Sale> = emptyList(),
    val saleItems : List<SaleItem> = emptyList(),

    val scannedText: String = "",

    val navigateToScanner: Boolean = false,

    val inventoryScannedText: String = "",
    val inventoryShowFormDialog: Boolean = false,

    val inventoryItemName: String = "",
    val inventoryItemPrice: String = "",
    val inventoryItemQuantity: String = "",
    val inventoryItemDescription: String = "",
    val inventoryItemColor: String = "",
    val inventoryItemCode: String = "",
    val inventoryItemSize: String = "",
    val inventoryItemSold: String = "",

)
