package com.example.firebaseapptest.ui.state

import com.example.firebaseapptest.data.local.entity.helpermodels.ItemForSale

data class InventoryState(
    val items : List<ItemForSale> = emptyList(),
    val scannedText: String = "",
    val showFormDialog: Boolean = false,

    val currentPage: Int = 1,
    val lastPage: Int = 1,

    val itemName: String = "",
    val itemPrice: String = "",
    val itemQuantity: String = "",
    val itemDescription: String = "",
    val itemColor: String = "",
    val itemCode: String = "",
    val itemSize: String = "",
    val itemSold: String = "",

    val deleteConfirmationDialog: Boolean = false,
)
