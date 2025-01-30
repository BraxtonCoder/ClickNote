package com.example.clicknote.ui.backup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.service.BackupSearchService.*
import com.example.clicknote.ui.components.DateRangeDialog
import com.example.clicknote.ui.components.LoadingScreen
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupBrowserScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupBrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf<BackupInfo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup Browser") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search backups...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                singleLine = true
            )

            if (uiState.isLoading) {
                LoadingScreen()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.backups) { backup ->
                        BackupItem(
                            backup = backup,
                            onRestore = { showRestoreDialog = backup },
                            onDelete = viewModel::deleteBackup
                        )
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Backups") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Date Range Filter
                    Text("Date Range", style = MaterialTheme.typography.titleMedium)
                    DateRangeFilterChips(
                        selected = uiState.filters.dateRange,
                        onSelected = { 
                            if (it == DateRange.CUSTOM) {
                                showDateRangeDialog = true
                            } else {
                                viewModel.updateDateRange(it)
                            }
                        }
                    )

                    // Backup Type Filter
                    Text("Backup Type", style = MaterialTheme.typography.titleMedium)
                    BackupTypeFilterChips(
                        selected = uiState.filters.backupType,
                        onSelected = viewModel::updateBackupType
                    )

                    // Size Filter
                    Text("Size", style = MaterialTheme.typography.titleMedium)
                    SizeFilterChips(
                        selected = uiState.filters.sizeFilter,
                        onSelected = viewModel::updateSizeFilter
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Date Range Dialog
    if (showDateRangeDialog) {
        DateRangeDialog(
            initialStartDate = uiState.filters.customStartDate,
            initialEndDate = uiState.filters.customEndDate,
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { start, end ->
                viewModel.updateCustomDateRange(start, end)
                showDateRangeDialog = false
            }
        )
    }

    // Restore Confirmation Dialog
    showRestoreDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Restore Backup") },
            text = { Text("Are you sure you want to restore the backup from ${backup.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreBackup(backup)
                        showRestoreDialog = null
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error Dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun BackupItem(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onDelete: (BackupInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = backup.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = backup.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatSize(backup.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Restore, "Restore")
                    }
                    IconButton(onClick = { onDelete(backup) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRangeFilterChips(
    selected: DateRange,
    onSelected: (DateRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == DateRange.ALL,
            onClick = { onSelected(DateRange.ALL) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selected == DateRange.TODAY,
            onClick = { onSelected(DateRange.TODAY) },
            label = { Text("Today") }
        )
        FilterChip(
            selected = selected == DateRange.LAST_7_DAYS,
            onClick = { onSelected(DateRange.LAST_7_DAYS) },
            label = { Text("Last 7 Days") }
        )
        FilterChip(
            selected = selected == DateRange.LAST_30_DAYS,
            onClick = { onSelected(DateRange.LAST_30_DAYS) },
            label = { Text("Last 30 Days") }
        )
        FilterChip(
            selected = selected == DateRange.CUSTOM,
            onClick = { onSelected(DateRange.CUSTOM) },
            label = { Text("Custom") }
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
} 