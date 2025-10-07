package com.example.firebaseapptest.ui.view.screen

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun CaptureTransactionAndCrop(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    navController: NavController
) {
    if (!state.isPaymentMethodChosen) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("Cash"))
            }) {
                Text("Cash")
            }
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("GCash"))

            }) {
                Text("GCash")
            }
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("Cash&GCash"))

            }) {
                Text("Cash & GCash")
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                text = state.paymentMethod,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            if (state.paymentMethod == "Cash&GCash") {
                TextField(
                    value = state.amountPaidCash,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filter { it.isDigit() }
                        onEvent(AppEvent.OnAmountPaidChanged(filteredValue, false))
                    },
                    label = { Text("Amount in Cash") },
                )

                TextField(
                    value = state.amountPaidGCash,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filter { it.isDigit() }
                        onEvent(AppEvent.OnAmountPaidChanged(filteredValue, true))
                    },
                    label = { Text("Amount in GCash") },
                )

                TextField(
                    value = state.gCashReference,
                    onValueChange = {
                        onEvent(AppEvent.OnGCashReferenceChanged(it))
                    },
                    label = { Text("Reference") },
                )
            } else {
                TextField(
                    value = if (state.paymentMethod == "GCash") state.amountPaidGCash else if (state.paymentMethod == "Cash") state.amountPaidCash else "",
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filter { it.isDigit() }

                        if (state.paymentMethod == "GCash") {
                            onEvent(AppEvent.OnAmountPaidChanged(filteredValue, true))
                        } else if (state.paymentMethod == "Cash") {
                            onEvent(AppEvent.OnAmountPaidChanged(filteredValue, false))
                        }
                    },
                    label = { Text("Amount") },
                )

                if (state.paymentMethod == "GCash") {
                    TextField(
                        value = state.gCashReference,
                        onValueChange = {
                            onEvent(AppEvent.OnGCashReferenceChanged(it))
                        },
                        label = { Text("Reference") },
                    )
                }

                if (state.paymentMethod == "Cash" &&  (state.amountPaidCash.toDoubleOrNull() ?: 0.0) > state.itemsInCounterTotalPrice ) {
                    Text(
                        text = "Change: ₱ ${(state.amountPaidCash.toDoubleOrNull() ?: 0.0) - state.itemsInCounterTotalPrice}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }



            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.9f)
                    .padding(14.dp)
            ) {

                val items = aggregateItemsWithPrice(state.itemsInCounter)

                if (state.itemsInCounter.isEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Scan Item(s)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        }
                    }
                } else {
                    items(items, key = { item -> item.item.code }) { item ->

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp)
                            ) {
                                Text(
                                    text = item.item.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 170.dp),
                                    maxLines = 1
                                )
                                val originalPrice =
                                    if (item.item.isDiscountPercentage) item.item.price / (1 - item.item.discount / 100) else item.item.price + item.item.discount
                                Text(
                                    text = "₱ $originalPrice * ${item.quantity}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "Discount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (item.item.isDiscountPercentage) "${item.item.discount} %" else "₱ ${item.item.discount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!state.itemsInCounter.isEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )
                            Text(
                                text = "₱ ${state.itemsInCounterTotalPrice}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onEvent(AppEvent.OnAddSale)
                        navController.navigate(Route.Home.path)
                    },
                    enabled =
                        if(state.paymentMethod == "Cash&GCash"){
                            state.gCashReference.trim().isNotEmpty() && (
                                ((state.amountPaidCash.toDoubleOrNull() ?: 0.0) + (state.amountPaidGCash.toDoubleOrNull() ?: 0.0)) >= state.itemsInCounterTotalPrice
                            )
                        } else if(state.paymentMethod == "GCash"){
                            state.gCashReference.trim().isNotEmpty() && (state.amountPaidGCash.toDoubleOrNull() ?: 0.0) >= state.itemsInCounterTotalPrice
                        } else {
                            (state.amountPaidCash.toDoubleOrNull() ?: 0.0) >= state.itemsInCounterTotalPrice
                        },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Finish")
                }
                Button(
                    onClick = {
                        onEvent(AppEvent.OnPaymentMethodBack)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            }
        }
    }
}


fun createTempImageFile(context: Context): File {
    val timestamp = System.currentTimeMillis()
    return File(context.cacheDir, "sale_$timestamp.jpg")
}