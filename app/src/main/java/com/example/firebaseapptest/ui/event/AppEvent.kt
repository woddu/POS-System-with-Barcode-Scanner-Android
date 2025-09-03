package com.example.firebaseapptest.ui.event

import com.example.firebaseapptest.ui.view.SalesFilter

sealed interface AppEvent {
    object OnScanButtonClickedFromHome : AppEvent
    data class OnBarcodeScanned(val text: String) : AppEvent
    object OnScannerConsumed : AppEvent

    object OnAddSale: AppEvent
    object OnCancelSale: AppEvent

    data class OnFilterSales(val salesFilter: SalesFilter, val betweenDates: Pair<Long, Long>?): AppEvent
    data class OnSaleDetails(val saleId: Int) : AppEvent
}