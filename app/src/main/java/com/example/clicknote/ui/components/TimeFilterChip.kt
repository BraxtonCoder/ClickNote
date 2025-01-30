package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class TimeFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    LAST_YEAR("Last Year"),
    CUSTOM("Custom Range")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterChips(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    onCustomRangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = {
                    if (filter == TimeFilter.CUSTOM) {
                        onCustomRangeClick()
                    } else {
                        onFilterSelected(filter)
                    }
                },
                label = { Text(filter.label) }
            )
        }
    }
} 