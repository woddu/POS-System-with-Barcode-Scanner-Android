package com.example.firebaseapptest.ui.view.screen.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.firebaseapptest.ui.state.AppState
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SaleDetails(
    state: AppState
){
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.ENGLISH)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(
                text = state.saleWithItemNames?.sale?.date?.format(formatter) ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = "Payment: ${state.saleWithItemNames?.sale?.paymentMethod}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Total: ₱ ${state.saleWithItemNames?.sale?.total}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            state.saleWithItemNames?.items?.forEachIndexed { index, item ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                ) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 170.dp),
                        maxLines = 1
                    )
                    Text(
                        text = "₱ ${item.price} * ${item.quantity}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val imageFileExists = remember(state.imageUri) {
                state.imageUri?.path?.let { File(it).exists() } ?: false
            }
            val context = LocalContext.current
            if (imageFileExists) {

                Text("Transaction: ")

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(state.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Captured image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

            }
        }
    }
}