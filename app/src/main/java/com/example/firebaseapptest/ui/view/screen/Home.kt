package com.example.firebaseapptest.ui.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.firebaseapptest.R
import com.example.firebaseapptest.data.local.entity.helpermodels.ItemForSale
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard
import kotlinx.coroutines.launch


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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
){
    var showDialog by remember { mutableStateOf(false)}

    var dropDownState by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(state.showSnackbar) {
        if (state.showSnackbar) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Item Not Found",
                    duration = SnackbarDuration.Short
                )
            }
        }
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
                    }
                ) {
                    Text(text = "Payment")
                }

                Box {
                    FilledIconButton(
                        onClick = {
                            dropDownState = true
                        },
                        modifier = Modifier
                            .size(56.dp)

                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.barcode_scanner_icon),
                            contentDescription = "Add"
                        )
                    }
                    DropdownMenu(
                        expanded = dropDownState,
                        onDismissRequest = { dropDownState = false },
                        offset = DpOffset(x = -(56.dp / 2), y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(
                                text = "Scan",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) },
                            onClick = {
                                onEvent(AppEvent.OnScanButtonClickedFromHome)
                                dropDownState = false
                            },

                            )
                        DropdownMenuItem(
                            text = { Text(
                                text = "Type",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) },
                            onClick = {
                                showDialog = true
                                dropDownState = false
                            }
                        )
                    }
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
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {


                Box {
                    FilledIconButton(
                        onClick = {
                            dropDownState = true
                        },
                        modifier = Modifier
                            .size(56.dp)

                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.barcode_scanner_icon),
                            contentDescription = "Add"
                        )
                    }
                    DropdownMenu(
                        expanded = dropDownState,
                        onDismissRequest = { dropDownState = false },
                        offset = DpOffset(x = -(56.dp / 2), y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(
                                text = "Scan",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) },
                            onClick = {
                                onEvent(AppEvent.OnScanButtonClickedFromHome)
                                dropDownState = false
                            },

                        )
                        DropdownMenuItem(
                            text = { Text(
                                text = "Type",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) },
                            onClick = {
                                showDialog = true
                                dropDownState = false
                            }
                        )
                    }
                }
            }
        }


    }

    if(showDialog){
        BasicAlertDialog(
            onDismissRequest = {
                showDialog = false
            }
        ){
            var itemCode by remember { mutableStateOf("") }
            SimpleCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .padding(26.dp)
                ) {
                    TextField(
                        value = itemCode,
                        onValueChange = { newVal ->
                            val filteredVal = newVal.filter { it.isDigit() }
                            itemCode = filteredVal
                        },
                        label = { Text("Code") },
                    )
                    Button(
                        onClick = {
                            if (itemCode.isNotEmpty() && itemCode.isNotBlank()){
                                onEvent(AppEvent.OnItemCodeTyped(itemCode))
                                showDialog = false
                            }
                        },
                        enabled = itemCode.isNotEmpty() && itemCode.isNotBlank()
                    ){
                        Text("Submit")
                    }
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