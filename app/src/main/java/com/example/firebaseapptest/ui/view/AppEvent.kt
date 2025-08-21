package com.example.firebaseapptest.ui.view

sealed interface AppEvent {
    object OnScanButtonClicked : AppEvent
    data class OnBarcodeScanned(val text: String) : AppEvent
    object OnScannerConsumed : AppEvent
    object OnScanComplete: AppEvent

    object OnInventoryAddButtonClicked: AppEvent
    object OnInventoryAddCanceled: AppEvent
    data class OnInventorySetItemName(val name: String): AppEvent
    data class OnInventorySetItemPrice(val price: String): AppEvent
    data class OnInventorySetItemQuantity(val quantity: String): AppEvent
    data class OnInventorySetItemDescription(val description: String): AppEvent
    data class OnInventorySetItemColor(val color: String): AppEvent
    data class OnInventorySetItemCode(val code: String): AppEvent
    data class OnInventorySetItemSize(val size: String): AppEvent
    data class OnInventorySetItemSold(val sold: String): AppEvent
    object OnInventoryAddConfirmed: AppEvent

}