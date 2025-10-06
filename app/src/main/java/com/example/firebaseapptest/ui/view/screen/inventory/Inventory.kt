package com.example.firebaseapptest.ui.view.screen.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebaseapptest.R
import com.example.firebaseapptest.ui.event.InventoryEvent
import com.example.firebaseapptest.ui.state.InventoryState
import com.example.firebaseapptest.ui.view.screen.Route
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inventory(
    navigateToScanner: Boolean,
    state: InventoryState,
    navController: NavController,
    onEvent: (InventoryEvent) -> Unit,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight(.92f)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                val searchQuery = remember { mutableStateOf("") }
                TextField(
                    value = searchQuery.value,
                    onValueChange = {
                        searchQuery.value = it
                        onEvent(InventoryEvent.OnSearchQueryChanged(it))
                    },
                    prefix = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    label = { Text("Search") },
                    suffix = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            modifier = Modifier.clickable {
                                searchQuery.value = ""
                                onEvent(InventoryEvent.OnSearchQueryChanged(""))
                            }
                        )
                    }
                )
                FilledIconButton(
                    onClick = { onEvent(InventoryEvent.OnInventoryAddButtonClicked) },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                }
            }
            if (state.items.isEmpty()) {
                Text(
                    text = "No items found",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    items(state.items, key = { item -> item.code }) { item ->
                        SimpleCard(
                            roundedCornerShape = 12.dp,
                            elevation = 8.dp
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp, start = 12.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    modifier = Modifier.fillMaxWidth(.5f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "₱ " + item.price.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontSize = 24.sp,
                                    )
                                    IconButton(onClick = {
                                        onEvent(InventoryEvent.OnInventoryItemDetails(item.code))
                                        navController.navigate(Route.InventoryDetails.path)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "details"
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight()
        ) {
            IconButton(
                onClick = { onEvent(InventoryEvent.OnInventoryPreviousPage) },
                enabled = state.currentPage > 1
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Page")
            }
            Text(state.currentPage.toString() + "/" + state.lastPage.toString())
            IconButton(
                onClick = { onEvent(InventoryEvent.OnInventoryNextPage) },
                enabled = state.currentPage < state.lastPage
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Page")
            }
        }
    }

    if (state.showFormDialog) {
            BasicAlertDialog(
                onDismissRequest = { onEvent(InventoryEvent.OnInventoryAddCanceled) },
                modifier = Modifier.padding(8.dp)
            ) {
                SimpleCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .padding(26.dp)
                    ) {
                        Text(
                            text = "Add Item",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )
                        Text(
                            text = "fields with * is required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            TextField(
                                value = state.itemCode,
                                onValueChange = { onEvent(InventoryEvent.OnInventorySetItemCode(it)) },
                                label = { Text("Code *") },
                                modifier = Modifier.fillMaxWidth(.75f)
                            )
                            FilledIconButton(
                                onClick = { onEvent(InventoryEvent.OnScanButtonClickedFromInventory) }
                            ) {
                                Icon(imageVector = ImageVector.vectorResource(R.drawable.barcode_scanner_icon), contentDescription = "Scan Barcode")
                            }
                        }
                        TextField(
                            value = state.itemName,
                            onValueChange = { onEvent(InventoryEvent.OnInventorySetItemName(it)) },
                            label = { Text("Name *") },
                        )
                        TextField(
                            value = state.itemPrice,
                            onValueChange = { newValue ->
                                val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                                onEvent(InventoryEvent.OnInventorySetItemPrice(filteredValue))
                            },
                            label = { Text("Price *") },
                        )
                        TextField(
                            value = state.itemDescription,
                            onValueChange = { onEvent(InventoryEvent.OnInventorySetItemDescription(it)) },
                            label = { Text("Description") },
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ){

                            TextField(
                                value = state.itemQuantity,
                                onValueChange = { newValue ->
                                    val filteredValue = newValue.filter { it.isDigit() }
                                    onEvent(InventoryEvent.OnInventorySetItemQuantity(filteredValue))
                                },
                                label = { Text("Quantity") },
                                modifier = Modifier.fillMaxWidth(.5f)
                            )
                            TextField(
                                value = state.itemSold,
                                onValueChange = { newValue ->
                                    val filteredValue = newValue.filter { it.isDigit() }
                                    onEvent(InventoryEvent.OnInventorySetItemSold(filteredValue))
                                },
                                label = { Text("Sold") },
                                modifier = Modifier.fillMaxWidth()
                            )

                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TextField(
                                value = state.itemDiscount,
                                onValueChange = { newValue ->
                                    val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                                    onEvent(InventoryEvent.OnInventorySetItemDiscount(filteredValue))
                                },
                                label = { Text("Discount") },
                                suffix = { if(state.itemIsDiscountPercentage) Text("%") else Text("₱") },
                                modifier = Modifier.fillMaxWidth(.5f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = state.itemIsDiscountPercentage,
                                    onCheckedChange = { onEvent(InventoryEvent.OnInventorySetItemIsDiscountPercentage) }
                                )
                                Text("Percentage")
                            }
                        }
                        Button(
                            onClick = {
                                if (state.itemCode.isNotEmpty() && state.itemName.isNotEmpty() && state.itemPrice.isNotEmpty()){
                                    onEvent(InventoryEvent.OnInventoryAddConfirmed)
                                }
                            },
                            enabled = state.itemCode.isNotEmpty() && state.itemName.isNotEmpty() && state.itemPrice.isNotEmpty()
                        ){
                            Text("Add")
                        }
                    }
                }
            }
        }

    // Side effect for navigation
    LaunchedEffect(navigateToScanner) {
        if (navigateToScanner) {
            navController.navigate(Route.Scanner.path)
            onEvent(InventoryEvent.OnScannerConsumed)
        }
    }
}

