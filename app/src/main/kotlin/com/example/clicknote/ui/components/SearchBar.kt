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
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    recentSearches: List<String>,
    onClearHistory: () -> Unit,
    onSearchHistoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var customEndDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(modifier = modifier) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
                onSearch(query)
                expanded = false
            },
            active = expanded,
            onActiveChange = { expanded = it },
            placeholder = { Text("Search notes...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onClearSearch()
                        expanded = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TimeFilterChips(
                        selectedFilter = timeFilter,
                        onFilterSelected = onTimeFilterChange,
                        onCustomRangeClick = { showDateRangeDialog = true }
                    )
                }

                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent Searches",
                                style = MaterialTheme.typography.titleSmall
                            )
                            TextButton(onClick = onClearHistory) {
                                Text("Clear")
                            }
                        }
                    }

                    items(recentSearches) { recentSearch ->
                        ListItem(
                            headlineContent = { Text(recentSearch) },
                            leadingContent = {
                                Icon(Icons.Default.History, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                onSearchHistoryClick(recentSearch)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDateRangeDialog) {
        DateRangeDialog(
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { startDate, endDate ->
                customStartDate = startDate
                customEndDate = endDate
                if (startDate != null && endDate != null) {
                    onTimeFilterChange(TimeFilter.CUSTOM)
                }
            },
            initialStartDate = customStartDate,
            initialEndDate = customEndDate
        )
    }
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