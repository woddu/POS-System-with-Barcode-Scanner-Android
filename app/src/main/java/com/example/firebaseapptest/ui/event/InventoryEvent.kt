package com.example.firebaseapptest.ui.event


sealed interface InventoryEvent {

    object OnScanButtonClickedFromInventory : InventoryEvent

    object OnScannerConsumed: InventoryEvent

    data class OnSearchQueryChanged(val searchTerm: String): InventoryEvent

    object OnInventoryNextPage: InventoryEvent
    object OnInventoryPreviousPage: InventoryEvent
    object OnInventoryFirstPage: InventoryEvent
    object OnInventoryLastPage: InventoryEvent
    data class OnInventoryPageChanged(val page: Int): InventoryEvent

    object OnInventoryAddButtonClicked: InventoryEvent
    object OnInventoryAddCanceled: InventoryEvent
    data class OnInventorySetItemName(val name: String): InventoryEvent
    data class OnInventorySetItemPrice(val price: String): InventoryEvent
    data class OnInventorySetItemQuantity(val quantity: String): InventoryEvent
    data class OnInventorySetItemDescription(val description: String): InventoryEvent
    data class OnInventorySetItemColor(val color: String): InventoryEvent
    data class OnInventorySetItemCode(val code: String): InventoryEvent
    data class OnInventorySetItemSize(val size: String): InventoryEvent
    data class OnInventorySetItemSold(val sold: String): InventoryEvent
    data class OnInventorySetItemDiscount(val discount: String): InventoryEvent
    object OnInventorySetItemIsDiscountPercentage: InventoryEvent
    object OnInventoryAddConfirmed: InventoryEvent

    data class OnInventoryItemDetails(val code: Long): InventoryEvent

    object OnInventoryEditConfirmed: InventoryEvent
    object OnInventoryDeleteWarning: InventoryEvent
    data class OnInventoryDeleteConfirmed(val code: Long): InventoryEvent
    object OnInventoryDeleteCanceled: InventoryEvent


}