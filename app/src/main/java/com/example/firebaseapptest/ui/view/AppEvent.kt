package com.example.firebaseapptest.ui.view

sealed interface AppEvent {
    object OnScanButtonClickedFromHome : AppEvent
    data class OnBarcodeScanned(val text: String) : AppEvent
    object OnScannerConsumed : AppEvent
    object OnScanComplete: AppEvent


}