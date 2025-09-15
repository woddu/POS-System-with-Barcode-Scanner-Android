package com.example.firebaseapptest.ui.view.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.firebaseapptest.R
import com.example.firebaseapptest.data.local.entity.helpermodels.ItemForSale
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard


data class ItemWithQuantityAndTotal(
    val item: ItemForSale,
    val quantity: Int
)

fun aggregateItemsWithPrice(items: List<ItemForSale>): List<ItemWithQuantityAndTotal> {
    return items
        .groupingBy { it.code }
        .eachCount()
        .map { (code, qty) ->
            val item = items.first { it.code == code }
            ItemWithQuantityAndTotal(
                item = item,
                quantity = qty
            )
        }
}


@Composable
fun Home(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    navController: NavHostController,
){
    if(state.itemNotFound){
        val context = LocalContext.current
        Toast.makeText(context, "Item not Found!", Toast.LENGTH_SHORT).show()
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        SimpleCard(
            roundedCornerShape = 12.dp,
            elevation = 8.dp,
            modifier = Modifier.fillMaxHeight(.9f)
        ) {
            val items = aggregateItemsWithPrice(state.itemsInCounter)
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.9f)
                ) {
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
                                Text(
                                    text = "₱ ${item.item.price} * ${item.quantity}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
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
                    ){
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
        if (!state.itemsInCounter.isEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        navController.navigate(Route.CaptureTransaction.path)
//                        onEvent(AppEvent.OnAddSale)
                    }
                ) {
//                    Text(text = "Finish")
                    Text(text = "Payment")
                }

                FilledIconButton(
                    onClick = {
                        onEvent(AppEvent.OnScanButtonClickedFromHome)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(ImageVector.vectorResource(R.drawable.barcode_scanner_icon), contentDescription = "Add")
                }

                Button(
                    onClick = {
                        onEvent(AppEvent.OnCancelSale)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = "Cancel")
                }
            }
        }
        if (state.itemsInCounter.isEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledIconButton(
                    onClick = {
                        onEvent(AppEvent.OnScanButtonClickedFromHome)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.barcode_scanner_icon),
                        contentDescription = "Add"
                    )
                }
            }
        }


    }

    // Side effect for navigation
    LaunchedEffect(state.navigateToScanner) {
        if (state.navigateToScanner) {
            navController.navigate(Route.Scanner.path)
            onEvent(AppEvent.OnScannerConsumed)
        }
    }
}