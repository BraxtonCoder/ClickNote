package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var year by remember { mutableStateOf(selectedDate?.year ?: LocalDate.now().year) }
    var month by remember { mutableStateOf(selectedDate?.monthValue ?: LocalDate.now().monthValue) }
    var day by remember { mutableStateOf(selectedDate?.dayOfMonth ?: LocalDate.now().dayOfMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Year Picker
                DatePickerSpinner(
                    value = year,
                    onValueChange = { year = it },
                    range = (1900..2100),
                    label = "Year"
                )

                // Month Picker
                DatePickerSpinner(
                    value = month,
                    onValueChange = { month = it },
                    range = (1..12),
                    label = "Month"
                )

                // Day Picker
                DatePickerSpinner(
                    value = day,
                    onValueChange = { day = it },
                    range = (1..31),
                    label = "Day"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val date = LocalDate.of(year, month, day)
                        onDateSelected(date)
                        onDismiss()
                    } catch (e: Exception) {
                        // Handle invalid date
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSpinner(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let { intValue ->
                        if (intValue in range) {
                            onValueChange(intValue)
                        }
                    }
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
        }
    }
} 