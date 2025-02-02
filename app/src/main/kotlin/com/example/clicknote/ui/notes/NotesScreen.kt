package com.example.clicknote.ui.notes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.DismissValue
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.ui.components.NavigationDrawerContent
import com.example.clicknote.ui.components.SearchBar
import com.example.clicknote.ui.components.RecordingOverlay
import java.time.format.DateTimeFormatter
import com.example.clicknote.ui.recording.RecordingScreen
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import java.time.format.FormatStyle
import com.example.clicknote.ui.components.NoteCard
import com.example.clicknote.ui.components.NoteItem
import com.example.clicknote.ui.components.CreateFolderDialog
import com.example.clicknote.ui.components.FolderOptionsDialog
import com.example.clicknote.ui.notes.TimeFilter
import com.example.clicknote.sync.SyncState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

val folderColors = listOf(
    Color(0xFFE57373), // Red
    Color(0xFFFFB74D), // Orange
    Color(0xFFFFF176), // Yellow
    Color(0xFFAED581), // Light Green
    Color(0xFF4DD0E1), // Cyan
    Color(0xFF9575CD), // Purple
    Color(0xFFF06292), // Pink
    Color(0xFF90A4AE)  // Blue Grey
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    onNavigateToNote: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showFolderOptionsDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isRefreshing = uiState.syncState is SyncState.Syncing
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.syncNotes() }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        if (uiState.selectedNotes.isEmpty()) {
                            Text(stringResource(R.string.app_name))
                        } else {
                            Text("${uiState.selectedNotes.size} selected")
                        }
                    },
                    navigationIcon = {
                        if (uiState.selectedNotes.isEmpty()) {
                            IconButton(onClick = { /* Open drawer */ }) {
                                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
                            }
                        } else {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear selection")
                            }
                        }
                    },
                    actions = {
                        if (uiState.selectedNotes.isEmpty()) {
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                            }
                        } else {
                            IconButton(onClick = { viewModel.copySelectedNotes() }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy selected")
                            }
                            IconButton(onClick = { viewModel.moveSelectedNotesToTrash() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                            }
                        }
                        // Sync status indicator
                        when (uiState.syncState) {
                            is SyncState.Syncing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            is SyncState.Error -> {
                                IconButton(onClick = { viewModel.syncNotes() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry sync",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            is SyncState.Success -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Synced",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            else -> {
                                IconButton(onClick = { viewModel.syncNotes() }) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sync"
                                    )
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.startRecording() },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (uiState.isRecording) "Stop recording" else "Start recording"
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::searchNotes,
                    onClearSearch = viewModel::clearSearch,
                    timeFilter = uiState.timeFilter,
                    onTimeFilterChange = viewModel::updateTimeFilter
                )

                if (uiState.isLoading) {
                    LoadingScreen()
                } else if (uiState.error != null) {
                    ErrorScreen(message = uiState.error!!)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pinnedNotes = uiState.notes.filter { it.isPinned }
                        val unpinnedNotes = uiState.notes.filter { !it.isPinned }

                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Pinned",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(
                                items = pinnedNotes.sortedByDescending { it.timestamp },
                                key = { it.id }
                            ) { note ->
                                val dismissState = rememberDismissState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == DismissValue.DismissedToStart) {
                                            viewModel.moveNoteToTrash(note.id)
                                            true
                                        } else false
                                    }
                                )
                                SwipeToDismiss(
                                    state = dismissState,
                                    background = {
                                        val color = MaterialTheme.colorScheme.error
                                        val scale by animateFloatAsState(
                                            if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                                        )
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.scale(scale)
                                            )
                                        }
                                    },
                                    dismissContent = {
                                        NoteItem(
                                            note = note,
                                            isSelected = note.id in uiState.selectedNotes,
                                            onClick = { 
                                                if (uiState.selectedNotes.isEmpty()) {
                                                    onNavigateToNote(note.id)
                                                } else {
                                                    viewModel.toggleNoteSelection(note.id)
                                                }
                                            },
                                            onLongClick = { viewModel.toggleNoteSelection(note.id) },
                                            onTogglePin = { viewModel.toggleNotePin(note.id) }
                                        )
                                    },
                                    directions = setOf(DismissDirection.EndToStart)
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }

                        if (unpinnedNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Other",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(
                                items = unpinnedNotes.sortedByDescending { it.timestamp },
                                key = { it.id }
                            ) { note ->
                                val dismissState = rememberDismissState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == DismissValue.DismissedToStart) {
                                            viewModel.moveNoteToTrash(note.id)
                                            true
                                        } else false
                                    }
                                )
                                SwipeToDismiss(
                                    state = dismissState,
                                    background = {
                                        val color = MaterialTheme.colorScheme.error
                                        val scale by animateFloatAsState(
                                            if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                                        )
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.scale(scale)
                                            )
                                        }
                                    },
                                    dismissContent = {
                                        NoteItem(
                                            note = note,
                                            isSelected = note.id in uiState.selectedNotes,
                                            onClick = { 
                                                if (uiState.selectedNotes.isEmpty()) {
                                                    onNavigateToNote(note.id)
                                                } else {
                                                    viewModel.toggleNoteSelection(note.id)
                                                }
                                            },
                                            onLongClick = { viewModel.toggleNoteSelection(note.id) },
                                            onTogglePin = { viewModel.toggleNotePin(note.id) }
                                        )
                                    },
                                    directions = setOf(DismissDirection.EndToStart)
                                )
                            }
                        }

                        if (pinnedNotes.isEmpty() && unpinnedNotes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No notes found",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Error snackbar
                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }

        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateFolderDialog = false },
                onConfirm = { name, color ->
                    viewModel.createFolder(name, Color(color))
                    showCreateFolderDialog = false
                }
            )
        }

        if (showFolderOptionsDialog && selectedFolder != null) {
            FolderOptionsDialog(
                folder = uiState.folders.find { it.id == selectedFolder }!!,
                onDismiss = {
                    showFolderOptionsDialog = false
                    selectedFolder = null
                },
                onRename = { folder, newName ->
                    viewModel.renameFolder(folder.id, newName)
                    showFolderOptionsDialog = false
                    selectedFolder = null
                },
                onDelete = { folder ->
                    viewModel.deleteFolder(folder.id)
                    showFolderOptionsDialog = false
                    selectedFolder = null
                }
            )
        }
    }
}

@Composable
private fun EmptyState(
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No notes yet",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a new note by tapping the + button",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCreateNote,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Note")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesList(
    notes: List<NoteWithFolder>,
    onNoteClick: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onMoveToTrash: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = notes,
            key = { it.note.id }
        ) { noteWithFolder ->
            NoteItem(
                note = noteWithFolder.note,
                onTogglePin = { onTogglePin(noteWithFolder.note.id) },
                onMoveToTrash = { onMoveToTrash(noteWithFolder.note.id) },
                onClick = { onNoteClick(noteWithFolder.note.id) }
            )
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .format(note.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (note.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (note.hasAudio) {
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = "Has audio",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = if (note.isPinned) "Unpin" else "Pin",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onLongClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Move to trash",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

enum class DateFilter(val displayName: String) {
    ALL("All"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months"),
    LAST_YEAR("Last year")
} 