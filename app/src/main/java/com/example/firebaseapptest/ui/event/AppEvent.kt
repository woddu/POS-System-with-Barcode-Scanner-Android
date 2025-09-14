package com.example.firebaseapptest.ui.event

import android.content.Context
import android.net.Uri
import com.example.firebaseapptest.ui.view.SalesFilter
import java.io.File

sealed interface AppEvent {
    object OnScanButtonClickedFromHome : AppEvent
    data class OnBarcodeScanned(val text: String) : AppEvent
    object OnScannerConsumed : AppEvent

    data class OnImageCropped(val uri: Uri) : AppEvent

    data class OnTempImageFileCreated(val photoFile: File) : AppEvent
    data class OnImageUriChanged(val uri: Uri?) : AppEvent

    data class OnChoosePaymentMethod(val method: String): AppEvent

    data class OnAddSale(val context: Context): AppEvent

    object OnCancelSale: AppEvent

    data class OnFilterSales(val salesFilter: SalesFilter, val betweenDates: Pair<Long, Long>?): AppEvent
    data class OnSaleDetails(val saleId: Int) : AppEvent
}