package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import com.example.clicknote.domain.model.TimeFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterDialog(
    onDismiss: () -> Unit,
    onConfirm: (startDate: LocalDateTime?, endDate: LocalDateTime?) -> Unit
) {
    var selectedOption by remember { mutableStateOf<DateRangeOption>(DateRangeOption.All) }
    var customStartDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var customEndDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_filter)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeOption.values().forEach { option ->
                    FilterChip(
                        selected = selectedOption == option,
                        onClick = {
                            selectedOption = option
                            if (option != DateRangeOption.Custom) {
                                val (start, end) = option.getDateRange()
                                onConfirm(start, end)
                                onDismiss()
                            } else {
                                showCustomDatePicker = true
                            }
                        },
                        label = { Text(option.label) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val (start, end) = selectedOption.getDateRange(customStartDate, customEndDate)
                    onConfirm(start, end)
                    onDismiss()
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )

    if (showCustomDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onConfirm = { start, end ->
                customStartDate = start
                customEndDate = end
                selectedOption = DateRangeOption.Custom
                showCustomDatePicker = false
            }
        )
    }
}

private enum class DateRangeOption(val label: String) {
    All("All"),
    Today("Today"),
    LastWeek("Last 7 days"),
    LastMonth("Last 30 days"),
    LastThreeMonths("Last 3 months"),
    LastSixMonths("Last 6 months"),
    LastYear("Last year"),
    Custom("Custom range");

    fun getDateRange(
        customStart: LocalDateTime? = null,
        customEnd: LocalDateTime? = null
    ): Pair<LocalDateTime?, LocalDateTime?> {
        val now = LocalDateTime.now()
        return when (this) {
            All -> Pair(null, null)
            Today -> Pair(
                now.truncatedTo(ChronoUnit.DAYS),
                now
            )
            LastWeek -> Pair(
                now.minusDays(7),
                now
            )
            LastMonth -> Pair(
                now.minusDays(30),
                now
            )
            LastThreeMonths -> Pair(
                now.minusMonths(3),
                now
            )
            LastSixMonths -> Pair(
                now.minusMonths(6),
                now
            )
            LastYear -> Pair(
                now.minusYears(1),
                now
            )
            Custom -> Pair(customStart, customEnd)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDateTime, end: LocalDateTime) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDateTime.now()) }
    var endDate by remember { mutableStateOf(LocalDateTime.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start date picker
                DatePicker(
                    state = rememberDatePickerState(),
                    title = { Text("Start Date") }
                )
                
                // End date picker
                DatePicker(
                    state = rememberDatePickerState(),
                    title = { Text("End Date") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(startDate, endDate)
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun FilterOption(
    title: String,
    filter: TimeFilter,
    selectedFilter: TimeFilter,
    onSelect: () -> Unit,
    noteCount: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = modifier.selectable(
            selected = selectedFilter == filter,
            onClick = onSelect,
            role = Role.RadioButton
        ),
        leadingContent = {
            AnimatedRadioButton(
                selected = selectedFilter == filter
            )
        },
        supportingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    val transition = updateTransition(
                        targetState = noteCount,
                        label = "count_transition"
                    )
                    
                    val animatedCount by transition.animateInt(
                        label = "count_animation",
                        transitionSpec = {
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        }
                    ) { count -> count }
                    
                    Text(
                        "$animatedCount notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
private fun AnimatedRadioButton(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "radio_scale"
    )
    
    RadioButton(
        selected = selected,
        onClick = null,
        modifier = modifier.scale(scale)
    )
} 