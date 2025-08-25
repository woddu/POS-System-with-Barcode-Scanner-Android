package com.example.firebaseapptest.ui.view.screen.components

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MyCardDefaults {
    @Composable
    fun cardColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest
    ): CardColors {
        return CardDefaults.cardColors(
            containerColor = containerColor
        )
    }
}