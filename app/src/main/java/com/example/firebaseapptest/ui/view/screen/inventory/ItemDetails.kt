package com.example.firebaseapptest.ui.view.screen.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseapptest.R
import com.example.firebaseapptest.ui.event.InventoryEvent
import com.example.firebaseapptest.ui.state.InventoryState
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetails(
    state: InventoryState,
    onEvent: (InventoryEvent) -> Unit,
    navigate: () -> Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        Text(
            text = "Details",
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
        ) {
            TextField(
                value = state.itemCode,
                onValueChange = { onEvent(InventoryEvent.OnInventorySetItemCode(it)) },
                label = { Text("Code *") },
                modifier = Modifier.fillMaxWidth(.75f)
            )
            FilledIconButton(
                onClick = { onEvent(InventoryEvent.OnScanButtonClickedFromInventory) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.barcode_scanner_icon),
                    contentDescription = "Scan Barcode"
                )
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
        ) {

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
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(onClick = {
                if (state.itemCode.isNotEmpty() && state.itemName.isNotEmpty() && state.itemPrice.isNotEmpty()) {
                    onEvent(InventoryEvent.OnInventoryEditConfirmed)
                    navigate()
                }
            }) {
                Text("Edit")
            }
            Button(
                onClick = {
                onEvent(InventoryEvent.OnInventoryDeleteWarning)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete")
            }
        }

    }
    if (state.deleteConfirmationDialog){
        BasicAlertDialog(
            onDismissRequest = { onEvent(InventoryEvent.OnInventoryDeleteCanceled) },
        ) {
            SimpleCard {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(vertical = 20.dp)
                ) {
                    Text(
                        text = "Are you sure you want to delete ${state.itemName}?",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Button(
                            onClick = {
                                onEvent(InventoryEvent.OnInventoryDeleteConfirmed(state.itemCode.toLong()))
                                navigate()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Delete")
                        }
                        Button(onClick = { onEvent(InventoryEvent.OnInventoryDeleteCanceled) }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}