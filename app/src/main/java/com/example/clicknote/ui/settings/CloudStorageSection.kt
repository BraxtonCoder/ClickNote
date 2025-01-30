package com.example.clicknote.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CloudStorageSection(
    isCloudSyncEnabled: Boolean,
    isOfflineModeEnabled: Boolean,
    storageUsage: Long,
    storageLimit: Long,
    onCloudSyncToggle: (Boolean) -> Unit,
    onOfflineModeToggle: (Boolean) -> Unit,
    onBackupNow: () -> Unit,
    onRestoreBackup: () -> Unit,
    onManageStorage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cloud Storage & Sync",
                style = MaterialTheme.typography.titleMedium
            )

            // Storage usage bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storage Usage",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${formatStorageSize(storageUsage)} / ${formatStorageSize(storageLimit)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (storageUsage.toFloat() / storageLimit).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Cloud sync toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cloud Sync",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Automatically sync notes across devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isCloudSyncEnabled,
                    onCheckedChange = onCloudSyncToggle
                )
            }

            // Offline mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Offline Mode",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Work offline and sync later",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isOfflineModeEnabled,
                    onCheckedChange = onOfflineModeToggle
                )
            }

            Divider()

            // Backup options
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleSmall
            )

            OutlinedButton(
                onClick = onBackupNow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backup Now")
            }

            OutlinedButton(
                onClick = onRestoreBackup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore from Backup")
            }

            TextButton(
                onClick = onManageStorage,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Manage Storage")
            }
        }
    }
}

private fun formatStorageSize(bytes: Long): String {
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