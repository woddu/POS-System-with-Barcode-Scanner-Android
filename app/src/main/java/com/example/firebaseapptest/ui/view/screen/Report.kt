package com.example.firebaseapptest.ui.view.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.SalesFilter
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Report(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var totalSales = 0
    var totalCashAmount = 0.0
    var totalGCashAmount = 0.0
    var totalGCashAndCashAmount = 0.0
    var totalSoldItems = 0

    state.report.forEach { saleWithItems ->
        when (saleWithItems.sale.paymentMethod) {
            "Cash" -> {
                totalCashAmount += saleWithItems.sale.total
            }

            "GCash" -> {
                totalGCashAmount += saleWithItems.sale.total
            }

            "GCash&Cash" -> {
                totalGCashAndCashAmount += saleWithItems.sale.total
            }
        }
        totalSoldItems += saleWithItems.items.size
        totalSales++
    }


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
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp)
        ) {
            if (state.reportStartDate != null && state.reportEndDate != null && state.reportFilter == SalesFilter.BETWEEN) {
                val startTimeLocal = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(state.reportStartDate),
                    ZoneId.systemDefault()
                )
                val endTimeLocal = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(state.reportEndDate),
                    ZoneId.systemDefault()
                )
                Text(
                    "${startTimeLocal.format(formatter)} - ${endTimeLocal.format(formatter)}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                )
            } else if (state.reportFilter == SalesFilter.TODAY) {
                Text(
                    "Today",
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
                        text = { Text("Today") },
                        onClick = {
                            onEvent(AppEvent.OnFilterReport(SalesFilter.TODAY, null))
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
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {


            Text(
                text = "Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )


            SimpleCard(
                roundedCornerShape = 12.dp,
                elevation = 8.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 12.dp)
                ) {
                    Text(
                        text = "Total Sales:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(.5f)
                    )
                    Text(
                        text = "$totalSales",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                }
            }
            SimpleCard(
                roundedCornerShape = 12.dp,
                elevation = 8.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 12.dp)
                ) {
                    Text(
                        text = "Total Cash Amount:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(.5f)
                    )
                    Text(
                        text = "₱ $totalCashAmount",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                }
            }
            SimpleCard(
                roundedCornerShape = 12.dp,
                elevation = 8.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 12.dp)
                ) {
                    Text(
                        text = "Total GCash Amount:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(.5f)
                    )
                    Text(
                        text = "₱ $totalGCashAmount",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                }
            }
            SimpleCard(
                roundedCornerShape = 12.dp,
                elevation = 8.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 12.dp)
                ) {
                    Text(
                        text = "Total GCash&Cash Amount:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(.5f)
                    )
                    Text(
                        text = "₱ $totalGCashAndCashAmount",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                }
            }
            SimpleCard(
                roundedCornerShape = 12.dp,
                elevation = 8.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 12.dp)
                ) {
                    Text(
                        text = "Total Sold Items:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(.5f)
                    )
                    Text(
                        text = "$totalSoldItems",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 24.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                }
            }
        }

        val context = LocalContext.current

        Button(
            onClick = {
                if (state.reportFilter == SalesFilter.BETWEEN && state.reportStartDate != null && state.reportEndDate != null) {
                    val startTimeLocal = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(state.reportStartDate),
                        ZoneId.systemDefault()
                    )
                    val endTimeLocal = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(state.reportEndDate),
                        ZoneId.systemDefault()
                    )
                    val file = generateReportPdf(
                        context,
                        startTimeLocal.format(formatter),
                        endTimeLocal.format(formatter),
                        totalSales,
                        totalCashAmount,
                        totalGCashAmount,
                        totalGCashAndCashAmount,
                        totalSoldItems
                    )
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "PDF saved at: ${file.absolutePath}",
                            actionLabel = "Open",
                            duration = SnackbarDuration.Long
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider", // must match your manifest provider authority
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            SnackbarResult.Dismissed -> { /* do nothing */ }
                        }

                    }
                } else {
                    val now = LocalDate.now()
                    val startOfDay = now.atStartOfDay(ZoneId.systemDefault())
                    val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                        .minusNanos(1)
                    val file = generateReportPdf(
                        context,
                        startOfDay.format(formatter),
                        endOfDay.format(formatter),
                        totalSales,
                        totalCashAmount,
                        totalGCashAmount,
                        totalGCashAndCashAmount,
                        totalSoldItems
                    )
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "PDF saved at: ${file.absolutePath}",
                            actionLabel = "Open",
                            duration = SnackbarDuration.Long
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider", // must match your manifest provider authority
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            SnackbarResult.Dismissed -> { /* do nothing */ }
                        }

                    }
                }
            },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Save as PDF")
        }
    }
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(
                        AppEvent.OnFilterReport(
                            SalesFilter.BETWEEN,
                            Pair(
                                startDate,
                                if (startDate == endDate) (endDate + (24 * 60 * 60 * 1000L) - 1L) else endDate
                            )
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


fun generateReportPdf(
    context: Context,
    startDate: String,
    endDate: String,
    totalSales: Int,
    totalCashAmount: Double,
    totalGCashAmount: Double,
    totalGCashAndCashAmount: Double,
    totalSoldItems: Int
): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)

    val canvas = page.canvas
    val paint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
    }

    var y = 50



    canvas.drawText("Report", 250f, y.toFloat(), paint)

    paint.isFakeBoldText = false
    paint.textSize = 16f

    y += 30
    canvas.drawText("Date: $startDate - $endDate", 50f, y.toFloat(), paint)

    y += 50
    canvas.drawText("Total Sales: $totalSales", 50f, y.toFloat(), paint)
    y += 30
    canvas.drawText("Total Cash Amount: ₱$totalCashAmount", 50f, y.toFloat(), paint)
    y += 30
    canvas.drawText("Total GCash Amount: ₱$totalGCashAmount", 50f, y.toFloat(), paint)
    y += 30
    canvas.drawText("Total GCash & Cash Amount: ₱$totalGCashAndCashAmount", 50f, y.toFloat(), paint)
    y += 30
    canvas.drawText("Total Sold Items: $totalSoldItems", 50f, y.toFloat(), paint)

    pdfDocument.finishPage(page)

    // Save into Documents/MyReports
    val documentsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val targetDir = File(documentsDir, "Sales Reports")
    if (!targetDir.exists()) targetDir.mkdirs()

    val file = File(targetDir, "report_$startDate-$endDate.pdf")
    FileOutputStream(file).use { out ->
        pdfDocument.writeTo(out)
    }

    pdfDocument.close()
    return file
}
