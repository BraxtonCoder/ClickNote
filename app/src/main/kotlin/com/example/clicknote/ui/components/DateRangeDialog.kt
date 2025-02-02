package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (startDate: LocalDate?, endDate: LocalDate?) -> Unit,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(startDate?.toString() ?: "Select Start Date")
                }

                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(endDate?.toString() ?: "Select End Date")
                }

                if (showStartDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { showStartDatePicker = false }) {
                                Text("OK")
                            }
                        }
                    ) {
                        DatePicker(
                            state = rememberDatePickerState(),
                            modifier = Modifier.padding(16.dp),
                            dateValidator = { timestamp ->
                                endDate?.let { 
                                    LocalDate.ofEpochDay(timestamp / 86400000).isBefore(it) 
                                } ?: true
                            }
                        )
                    }
                }

                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { showEndDatePicker = false }) {
                                Text("OK")
                            }
                        }
                    ) {
                        DatePicker(
                            state = rememberDatePickerState(),
                            modifier = Modifier.padding(16.dp),
                            dateValidator = { timestamp ->
                                startDate?.let { 
                                    LocalDate.ofEpochDay(timestamp / 86400000).isAfter(it) 
                                } ?: true
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(startDate, endDate)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 