package com.example.firebaseapptest.ui.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.firebaseapptest.ui.view.AppEvent

@Composable
fun Home(
    scannedText: String,
    navigateToScanner: Boolean,
    onEvent: (AppEvent) -> Unit,
    onNavigateToScanner: () -> Unit
){
    Column {
        Text("Scanned: $scannedText")
        Button(onClick = { onEvent(AppEvent.OnScanButtonClicked) }) {
            Text("Open Scanner")
        }
    }

    // Side effect for navigation
    LaunchedEffect(navigateToScanner) {
        if (navigateToScanner) {
            onNavigateToScanner()
            onEvent(AppEvent.OnScannerConsumed)
        }
    }
}