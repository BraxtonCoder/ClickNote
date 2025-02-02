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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.service.BackupInfo
import com.example.clicknote.ui.components.FileSelectionDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showBackupIntervalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.automatic_backups),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Switch(
                            checked = uiState.isAutoBackupEnabled,
                            onCheckedChange = { viewModel.toggleAutoBackup(it) }
                        )
                        if (uiState.isAutoBackupEnabled) {
                            TextButton(onClick = { showBackupIntervalDialog = true }) {
                                Text(stringResource(R.string.backup_interval_hours, uiState.backupIntervalHours))
                            }
                        }
                    }
                }
            }

            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.manual_backup),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = { showExportDialog = true }) {
                                Text(stringResource(R.string.create_backup))
                            }
                            Button(onClick = { showImportDialog = true }) {
                                Text(stringResource(R.string.restore_backup))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.backup_history),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(uiState.backups) { backup ->
                BackupItem(
                    backup = backup,
                    onDelete = { viewModel.deleteBackup(backup.path) },
                    onRestore = { viewModel.restoreBackup(backup.path) }
                )
            }
        }

        if (showExportDialog) {
            FileSelectionDialog(
                onDismiss = { showExportDialog = false },
                onFileSelected = { uri -> viewModel.exportBackup(uri) },
                isExport = true,
                fileType = "application/zip"
            )
        }

        if (showImportDialog) {
            FileSelectionDialog(
                onDismiss = { showImportDialog = false },
                onFileSelected = { uri -> viewModel.importBackup(uri) },
                isExport = false,
                fileType = "application/zip"
            )
        }

        if (showBackupIntervalDialog) {
            BackupIntervalDialog(
                currentInterval = uiState.backupIntervalHours,
                onDismiss = { showBackupIntervalDialog = false },
                onIntervalSelected = { 
                    viewModel.updateBackupInterval(it)
                    showBackupIntervalDialog = false
                }
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text(stringResource(R.string.error)) },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
private fun BackupItem(
    backup: BackupInfo,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = backup.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(Date(backup.createdAt)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatSize(backup.size),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun BackupIntervalDialog(
    currentInterval: Int,
    onDismiss: () -> Unit,
    onIntervalSelected: (Int) -> Unit
) {
    val intervals = listOf(1, 3, 6, 12, 24, 48, 72)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_interval)) },
        text = {
            Column {
                intervals.forEach { hours ->
                    TextButton(
                        onClick = { onIntervalSelected(hours) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.backup_interval_hours, hours),
                            color = if (hours == currentInterval) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.1f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> String.format("%d B", bytes)
    }
} 