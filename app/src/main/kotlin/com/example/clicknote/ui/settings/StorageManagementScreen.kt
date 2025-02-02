package com.example.clicknote.ui.settings

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: StorageManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Overview Card
            item {
                StorageOverviewCard(
                    totalUsage = uiState.totalStorageUsage,
                    limit = uiState.storageLimit,
                    usageByType = uiState.usageByType
                )
            }

            // Cloud Storage Provider Card
            item {
                CloudStorageProviderCard(
                    currentProvider = uiState.cloudStorageProvider,
                    onProviderSelect = viewModel::updateCloudStorageProvider
                )
            }

            // Backup Schedule Card
            item {
                BackupScheduleCard(
                    currentSchedule = uiState.backupSchedule,
                    onScheduleUpdate = viewModel::updateBackupSchedule,
                    isAutoBackupEnabled = uiState.isAutoBackupEnabled,
                    onAutoBackupToggle = viewModel::toggleAutoBackup
                )
            }

            // Data Management Card
            item {
                DataManagementCard(
                    onExportData = viewModel::exportData,
                    onImportData = viewModel::importData,
                    onClearCache = viewModel::clearCache,
                    onDeleteAllData = viewModel::showDeleteConfirmation
                )
            }
        }
    }

    // Error Dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Delete All Data") },
            text = { Text("Are you sure you want to delete all data? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmDelete()
                        viewModel.hideDeleteConfirmation()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageOverviewCard(
    totalUsage: Long,
    limit: Long,
    usageByType: Map<String, Long>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Storage Overview",
                style = MaterialTheme.typography.titleMedium
            )

            // Total storage usage bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Usage")
                    Text("${formatStorageSize(totalUsage)} / ${formatStorageSize(limit)}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (totalUsage.toFloat() / limit).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Usage by type
            usageByType.forEach { (type, usage) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(type)
                    Text(formatStorageSize(usage))
                }
            }
        }
    }
}

@Composable
private fun CloudStorageProviderCard(
    currentProvider: CloudStorageProvider,
    onProviderSelect: (CloudStorageProvider) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cloud Storage Provider",
                style = MaterialTheme.typography.titleMedium
            )

            CloudStorageProvider.values().forEach { provider ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = provider == currentProvider,
                            onClick = { onProviderSelect(provider) }
                        )
                        Text(provider.displayName)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupScheduleCard(
    currentSchedule: BackupSchedule,
    onScheduleUpdate: (BackupSchedule) -> Unit,
    isAutoBackupEnabled: Boolean,
    onAutoBackupToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Backup Schedule",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto Backup")
                Switch(
                    checked = isAutoBackupEnabled,
                    onCheckedChange = onAutoBackupToggle
                )
            }

            if (isAutoBackupEnabled) {
                BackupSchedule.values().forEach { schedule ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = schedule == currentSchedule,
                                onClick = { onScheduleUpdate(schedule) }
                            )
                            Text(schedule.displayName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataManagementCard(
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onClearCache: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Data")
            }

            OutlinedButton(
                onClick = onImportData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Data")
            }

            OutlinedButton(
                onClick = onClearCache,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Cache")
            }

            Button(
                onClick = onDeleteAllData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete All Data")
            }
        }
    }
} 