package com.example.clicknote.ui.sync

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.repository.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    lastSyncTime: Long,
    pendingNotesCount: Int,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedContent(
            targetState = syncStatus,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "sync_status"
        ) { status ->
            when (status) {
                SyncStatus.IDLE -> {
                    if (pendingNotesCount > 0) {
                        IconButton(onClick = onSyncClick) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sync needed",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                SyncStatus.SYNCING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                SyncStatus.ERROR -> {
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Sync error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                SyncStatus.SUCCESS -> {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Sync successful",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when (syncStatus) {
                    SyncStatus.IDLE -> if (pendingNotesCount > 0) {
                        "$pendingNotesCount notes pending sync"
                    } else {
                        "All notes synced"
                    }
                    SyncStatus.SYNCING -> "Syncing..."
                    SyncStatus.ERROR -> "Sync failed"
                    SyncStatus.SUCCESS -> "Sync successful"
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (lastSyncTime > 0) {
                Text(
                    text = "Last synced: ${formatLastSyncTime(lastSyncTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatLastSyncTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
} 