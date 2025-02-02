package com.example.clicknote.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.components.NoteItem

@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel(),
    onNoteClick: (String) -> Unit
) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val pendingNotes by viewModel.pendingNotes.collectAsState()
    val syncError by viewModel.syncError.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SyncStatusIndicator(
            syncStatus = syncStatus,
            lastSyncTime = lastSyncTime,
            pendingNotesCount = pendingNotes.size,
            onSyncClick = viewModel::syncNotes,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (pendingNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All notes are synced",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(pendingNotes, key = { it.id }) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
        }
    }

    syncError?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            // You'll need to implement your own snackbar handling
            viewModel.clearError()
        }
    }
} 