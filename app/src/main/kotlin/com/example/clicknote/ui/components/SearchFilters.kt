package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.DateRange
import com.example.clicknote.domain.model.Folder
import java.time.LocalDateTime

data class SearchFilters(
    val query: String = "",
    val dateRange: DateRange? = null,
    val folders: List<Folder> = emptyList(),
    val hasAudio: Boolean? = null,
    val hasSummary: Boolean? = null,
    val sortBy: SortOption = SortOption.DATE_MODIFIED_DESC
)

enum class SortOption {
    DATE_CREATED_ASC,
    DATE_CREATED_DESC,
    DATE_MODIFIED_ASC,
    DATE_MODIFIED_DESC,
    TITLE_ASC,
    TITLE_DESC,
    LENGTH_ASC,
    LENGTH_DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFiltersBar(
    filters: SearchFilters,
    onFiltersChanged: (SearchFilters) -> Unit,
    folders: List<Folder>,
    modifier: Modifier = Modifier
) {
    var showFiltersDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Search bar with filter button
        OutlinedTextField(
            value = filters.query,
            onValueChange = { onFiltersChanged(filters.copy(query = it)) },
            placeholder = { Text("Search notes") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                Row {
                    if (filters.query.isNotEmpty()) {
                        IconButton(onClick = { onFiltersChanged(filters.copy(query = "")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                    IconButton(onClick = { showFiltersDialog = true }) {
                        Badge(
                            modifier = Modifier.padding(end = 4.dp),
                            content = {
                                val activeFilters = listOfNotNull(
                                    filters.dateRange,
                                    filters.folders.takeIf { it.isNotEmpty() },
                                    filters.hasAudio,
                                    filters.hasSummary
                                ).size
                                if (activeFilters > 0) {
                                    Text(activeFilters.toString())
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
                    }
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Active filters chips
        if (filters.hasActiveFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.dateRange?.let { dateRange ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            onFiltersChanged(filters.copy(dateRange = null))
                        },
                        label = { Text(dateRange.toString()) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                if (filters.folders.isNotEmpty()) {
                    FilterChip(
                        selected = true,
                        onClick = {
                            onFiltersChanged(filters.copy(folders = emptyList()))
                        },
                        label = {
                            Text(
                                if (filters.folders.size == 1) {
                                    filters.folders.first().name
                                } else {
                                    "${filters.folders.size} folders"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                filters.hasAudio?.let { hasAudio ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            onFiltersChanged(filters.copy(hasAudio = null))
                        },
                        label = {
                            Text(if (hasAudio) "Has audio" else "No audio")
                        },
                        leadingIcon = {
                            Icon(
                                if (hasAudio) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                filters.hasSummary?.let { hasSummary ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            onFiltersChanged(filters.copy(hasSummary = null))
                        },
                        label = {
                            Text(if (hasSummary) "Has summary" else "No summary")
                        },
                        leadingIcon = {
                            Icon(
                                if (hasSummary) Icons.Default.AutoAwesome else Icons.Default.AutoAwesomeMotion,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
    
    // Filters dialog
    if (showFiltersDialog) {
        AlertDialog(
            onDismissRequest = { showFiltersDialog = false },
            title = { Text("Filter Notes") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date range
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleSmall
                    )
                    DateRangePicker(
                        selectedRange = filters.dateRange,
                        onRangeSelected = {
                            onFiltersChanged(filters.copy(dateRange = it))
                        }
                    )
                    
                    Divider()
                    
                    // Folders
                    Text(
                        text = "Folders",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        folders.forEach { folder ->
                            FilterChip(
                                selected = folder in filters.folders,
                                onClick = {
                                    val newFolders = if (folder in filters.folders) {
                                        filters.folders - folder
                                    } else {
                                        filters.folders + folder
                                    }
                                    onFiltersChanged(filters.copy(folders = newFolders))
                                },
                                label = { Text(folder.name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    Divider()
                    
                    // Audio filter
                    Text(
                        text = "Audio",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filters.hasAudio == true,
                            onClick = {
                                onFiltersChanged(
                                    filters.copy(
                                        hasAudio = if (filters.hasAudio == true) null else true
                                    )
                                )
                            },
                            label = { Text("Has audio") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = filters.hasAudio == false,
                            onClick = {
                                onFiltersChanged(
                                    filters.copy(
                                        hasAudio = if (filters.hasAudio == false) null else false
                                    )
                                )
                            },
                            label = { Text("No audio") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.MicOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    Divider()
                    
                    // Summary filter
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filters.hasSummary == true,
                            onClick = {
                                onFiltersChanged(
                                    filters.copy(
                                        hasSummary = if (filters.hasSummary == true) null else true
                                    )
                                )
                            },
                            label = { Text("Has summary") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = filters.hasSummary == false,
                            onClick = {
                                onFiltersChanged(
                                    filters.copy(
                                        hasSummary = if (filters.hasSummary == false) null else false
                                    )
                                )
                            },
                            label = { Text("No summary") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesomeMotion,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFiltersDialog = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onFiltersChanged(SearchFilters())
                        showFiltersDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            }
        )
    }
    
    // Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort By") },
            text = {
                Column {
                    SortOption.values().forEach { option ->
                        RadioButton(
                            selected = filters.sortBy == option,
                            onClick = {
                                onFiltersChanged(filters.copy(sortBy = option))
                                showSortDialog = false
                            },
                            label = {
                                Text(
                                    when (option) {
                                        SortOption.DATE_CREATED_ASC -> "Date Created (Oldest First)"
                                        SortOption.DATE_CREATED_DESC -> "Date Created (Newest First)"
                                        SortOption.DATE_MODIFIED_ASC -> "Date Modified (Oldest First)"
                                        SortOption.DATE_MODIFIED_DESC -> "Date Modified (Newest First)"
                                        SortOption.TITLE_ASC -> "Title (A to Z)"
                                        SortOption.TITLE_DESC -> "Title (Z to A)"
                                        SortOption.LENGTH_ASC -> "Length (Shortest First)"
                                        SortOption.LENGTH_DESC -> "Length (Longest First)"
                                    }
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            label()
        }
    }
}

private val SearchFilters.hasActiveFilters: Boolean
    get() = dateRange != null || folders.isNotEmpty() || hasAudio != null || hasSummary != null 