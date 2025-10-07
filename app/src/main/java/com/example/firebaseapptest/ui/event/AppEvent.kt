package com.example.firebaseapptest.ui.event

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

    object OnAddSale: AppEvent

    object OnCancelSale: AppEvent

    data class OnFilterSales(val salesFilter: SalesFilter, val betweenDates: Pair<Long, Long>?): AppEvent
    data class OnSaleDetails(val saleId: Int) : AppEvent

    data class OnAmountPaidChanged(val amount: String, val isGash: Boolean) : AppEvent

    data class OnGCashReferenceChanged(val reference: String) : AppEvent

    data class OnItemCodeTyped(val code: String) : AppEvent

    object OnPaymentMethodBack : AppEvent

    data class OnFilterReport(val reportFilter: SalesFilter, val reportBetweenDates: Pair<Long, Long>?): AppEvent
}

