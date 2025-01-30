package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

sealed class DateRange(val label: String) {
    data object All : DateRange("All")
    data object Today : DateRange("Today")
    data object LastWeek : DateRange("Last 7 days")
    data object LastMonth : DateRange("Last 30 days")
    data object Last3Months : DateRange("Last 3 months")
    data object Last6Months : DateRange("Last 6 months")
    data object LastYear : DateRange("Last year")
    data class Custom(
        val startDate: LocalDate,
        val endDate: LocalDate
    ) : DateRange("Custom")

    fun getDateRange(): Pair<LocalDate, LocalDate>? {
        val today = LocalDate.now()
        return when (this) {
            is All -> null
            is Today -> Pair(today, today)
            is LastWeek -> Pair(today.minusDays(7), today)
            is LastMonth -> Pair(today.minusDays(30), today)
            is Last3Months -> Pair(today.minusMonths(3), today)
            is Last6Months -> Pair(today.minusMonths(6), today)
            is LastYear -> Pair(today.minusYears(1), today)
            is Custom -> Pair(startDate, endDate)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf(LocalDate.now()) }
    var customEndDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    // Filter button
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(selectedRange.label)
    }

    // Date range dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Filter by date") },
            text = {
                Column {
                    listOf(
                        DateRange.All,
                        DateRange.Today,
                        DateRange.LastWeek,
                        DateRange.LastMonth,
                        DateRange.Last3Months,
                        DateRange.Last6Months,
                        DateRange.LastYear
                    ).forEach { range ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRange::class == range::class,
                                onClick = {
                                    onRangeSelected(range)
                                    showDialog = false
                                }
                            )
                            Text(
                                text = range.label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    
                    // Custom date range option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRange is DateRange.Custom,
                            onClick = {
                                showCustomDatePicker = true
                                showDialog = false
                            }
                        )
                        Text(
                            text = if (selectedRange is DateRange.Custom) {
                                "${selectedRange.startDate.format(dateFormatter)} - ${selectedRange.endDate.format(dateFormatter)}"
                            } else "Custom range",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Custom date range picker dialog
    if (showCustomDatePicker) {
        AlertDialog(
            onDismissRequest = { showCustomDatePicker = false },
            title = { Text("Select date range") },
            text = {
                Column {
                    // Start date picker
                    OutlinedButton(
                        onClick = {
                            // TODO: Show material date picker for start date
                        }
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start: ${customStartDate.format(dateFormatter)}")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // End date picker
                    OutlinedButton(
                        onClick = {
                            // TODO: Show material date picker for end date
                        }
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("End: ${customEndDate.format(dateFormatter)}")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRangeSelected(DateRange.Custom(customStartDate, customEndDate))
                        showCustomDatePicker = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 