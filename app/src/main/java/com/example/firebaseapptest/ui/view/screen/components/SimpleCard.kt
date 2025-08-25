package com.example.firebaseapptest.ui.view.screen.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SimpleCard(
    modifier: Modifier = Modifier,
    cardColors: CardColors = MyCardDefaults.cardColors(),
    roundedCornerShape: Dp = 24.dp,
    elevation: Dp = 24.dp,
    content: @Composable () -> Unit
    ){
    Card(
        colors = cardColors,
        shape = RoundedCornerShape(roundedCornerShape),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        modifier = modifier
    ) {
        content()
    }
}