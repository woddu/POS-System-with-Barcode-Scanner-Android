package com.example.firebaseapptest.ui.view.screen.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.SalesFilter
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sale(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    navController: NavController
) {

    var showDialog by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    var dropDownState by remember { mutableStateOf(false) }
    val startDate = dateRangePickerState.selectedStartDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()) // local midnight
            .toInstant()
            .toEpochMilli() // back to Long
    } ?: 0

    val endDate = dateRangePickerState.selectedEndDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atTime(LocalTime.MAX) // 23:59:59.999999999
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } ?: 0

    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp)
        ){
            if (state.startDate != null && state.endDate != null && state.salesFilter == SalesFilter.BETWEEN){
                val startTimeLocal = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(state.startDate),
                    ZoneId.systemDefault()
                )
                val endTimeLocal = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(state.endDate),
                    ZoneId.systemDefault()
                )
                Text(
                    "${startTimeLocal.format(formatter)} - ${endTimeLocal.format(formatter)}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            } else if (state.salesFilter == SalesFilter.TODAY){
                Text(
                    "Today",
                    modifier = Modifier.padding(8.dp),

                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            } else if (state.salesFilter == SalesFilter.ALL){
                Text(
                    "All",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            }
            Box {
                Button(onClick = {
                    dropDownState = true
                }) {
                    Text("Filter")
                }
                DropdownMenu(
                    expanded = dropDownState,
                    onDismissRequest = { dropDownState = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onEvent(AppEvent.OnFilterSales(SalesFilter.ALL, null))
                            dropDownState = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Today") },
                        onClick = {
                            onEvent(AppEvent.OnFilterSales(SalesFilter.TODAY, null))
                            dropDownState = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Date Range") },
                        onClick = {
                            showDialog = true
                            dropDownState = false
                        }
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.9f)
                .padding(top = 8.dp)
        ) {
            if (state.sales.isEmpty())
                item {
                    Text(
                        text = "Empty",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                }
            else
                items(state.sales, key = { sale -> sale.id }) { sale ->
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
                                text = sale.date.format(formatter),
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
                                    text = "₱ " + sale.total.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 24.sp,
                                )
                                IconButton(onClick = {
                                    onEvent(AppEvent.OnSaleDetails(sale.id))
                                    navController.navigate("saleDetails")
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


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {  },
                enabled = state.currentPage > 1
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Page")
            }
            Text(state.currentPage.toString() + "/" + state.lastPage.toString())
            IconButton(
                onClick = {  },
                enabled = state.currentPage < state.lastPage
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Page")
            }
        }
    }
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(AppEvent.OnFilterSales(
                        SalesFilter.BETWEEN,
                        Pair(
                            startDate,
                            if(startDate == endDate) (endDate + (24 * 60 * 60 * 1000L) - 1L) else endDate)
                        )
                    )
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState)
        }
    }
}

