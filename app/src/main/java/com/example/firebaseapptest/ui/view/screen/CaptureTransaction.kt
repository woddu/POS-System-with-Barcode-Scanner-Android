package com.example.firebaseapptest.ui.view.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureTransactionAndCrop(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    navController: NavController
) {
    val bargainChosenItemCode = remember { mutableStateOf<Long?>(null) }
    val bargainChosenItemPrice = remember { mutableStateOf<Double?>(null) }
    val showBargainDialog = remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxHeight(.9f)
                    .padding(12.dp)
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

                    if (state.paymentMethod == "Cash" && (state.amountPaidCash.toDoubleOrNull()
                            ?: 0.0) > state.itemsInCounterTotalPrice
                    ) {
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
                        .fillMaxHeight(.8f)
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (item.item.isDiscountPercentage) "${item.item.discount} %" else "₱ ${item.item.discount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        IconButton(
                                            onClick = {
                                                bargainChosenItemCode.value = item.item.code
                                                bargainChosenItemPrice.value = item.item.price
                                                showBargainDialog.value = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Bargain"
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }


                }
                if (!state.itemsInCounter.isEmpty()) {
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
                        when (state.paymentMethod) {
                            "Cash&GCash" -> {
                                state.gCashReference.trim().isNotEmpty() && (
                                        ((state.amountPaidCash.toDoubleOrNull() ?: 0.0) + (state.amountPaidGCash.toDoubleOrNull() ?: 0.0)) >= state.itemsInCounterTotalPrice
                                        )
                            }
                            "GCash" -> {
                                state.gCashReference.trim().isNotEmpty() && (state.amountPaidGCash.toDoubleOrNull() ?: 0.0) >= state.itemsInCounterTotalPrice
                            }
                            else -> {
                                (state.amountPaidCash.toDoubleOrNull() ?: 0.0) >= state.itemsInCounterTotalPrice
                            }
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

    if(showBargainDialog.value && bargainChosenItemCode.value != null && bargainChosenItemPrice.value != null){
        BasicAlertDialog(
            onDismissRequest = {
                showBargainDialog.value = false
                bargainChosenItemCode.value = null
            }
        ) {
            var bargain by remember { mutableStateOf("0") }
            SimpleCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .padding(26.dp)
                ) {
                    TextField(
                        value = bargain,
                        onValueChange = { newVal ->
                            val filteredVal = newVal.filter { it.isDigit() }
                            bargain = filteredVal
                        },
                        label = { Text("Bargain") },
                    )
                    Button(
                        onClick = {
                            if (bargain.isNotEmpty() && bargain.isNotBlank()) {
                                onEvent(
                                    AppEvent.OnItemBargain(
                                        bargainChosenItemCode.value!!,
                                        bargain.toDouble()
                                    )
                                )
                                showBargainDialog.value = false
                                bargainChosenItemCode.value = null
                            }
                        },
                        enabled = bargain.isNotEmpty() && bargain.isNotBlank() && (
                                    bargainChosenItemPrice.value!!  > (bargain.toDoubleOrNull() ?: 0.0)
                                )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}


fun createTempImageFile(context: Context): File {
    val timestamp = System.currentTimeMillis()
    return File(context.cacheDir, "sale_$timestamp.jpg")
}