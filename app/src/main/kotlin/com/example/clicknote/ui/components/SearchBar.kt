package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.clicknote.R
import com.example.clicknote.data.model.SearchHistory
import com.example.clicknote.domain.model.TimeFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimeFilterDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (showTimeFilterDialog) {
            TimeFilterDialog(
                currentFilter = timeFilter,
                onFilterChange = { filter ->
                    onTimeFilterChange(filter)
                    showTimeFilterDialog = false
                },
                onDismiss = { showTimeFilterDialog = false }
            )
        }
    }
}

@Composable
private fun TimeFilterDialog(
    currentFilter: TimeFilter,
    onFilterChange: (TimeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_by_time)) },
        text = {
            Column {
                TimeFilter.values().forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filter == currentFilter,
                            onClick = { onFilterChange(filter) }
                        )
                        Text(
                            text = when (filter) {
                                TimeFilter.ALL -> stringResource(R.string.all_time)
                                TimeFilter.TODAY -> stringResource(R.string.today)
                                TimeFilter.LAST_7_DAYS -> stringResource(R.string.last_7_days)
                                TimeFilter.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                TimeFilter.LAST_3_MONTHS -> stringResource(R.string.last_3_months)
                                TimeFilter.LAST_6_MONTHS -> stringResource(R.string.last_6_months)
                                TimeFilter.LAST_YEAR -> stringResource(R.string.last_year)
                                is TimeFilter.CUSTOM -> stringResource(R.string.custom_range)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun DateRangeFilterDialog(
    currentFilter: TimeFilter,
    onFilterChange: (TimeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation of DateRangeFilterDialog
}

@Composable
private fun DateFilterMenu(
    currentFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        TimeFilter.values().forEach { filter ->
            DropdownMenuItem(
                text = { Text(stringResource(filter.labelResId)) },
                onClick = { onFilterSelected(filter) },
                leadingIcon = {
                    if (filter == currentFilter) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    }
} 