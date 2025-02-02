package com.example.clicknote.ui.notes

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.ui.components.*
import com.example.clicknote.ui.note.NoteDetailTab
import com.example.clicknote.ui.note.NoteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateUp: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(noteId) {
        viewModel.loadNoteById(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, "Navigate back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleNotePin() }
                    ) {
                        Icon(
                            imageVector = if (uiState.note?.isPinned == true) 
                                Icons.Default.PushPin else Icons.Filled.PushPin,
                            contentDescription = if (uiState.note?.isPinned == true) 
                                "Unpin note" else "Pin note"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.copyNoteToClipboard() }
                    ) {
                        Icon(Icons.Default.ContentCopy, "Copy note")
                    }
                    IconButton(
                        onClick = { viewModel.moveNoteToTrash() }
                    ) {
                        Icon(Icons.Default.Delete, "Move to trash")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingScreen()
            uiState.error != null -> ErrorScreen(message = uiState.error!!)
            uiState.note == null -> ErrorScreen(message = "Note not found")
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabRow(
                        selectedTabIndex = uiState.currentTab.ordinal
                    ) {
                        Tab(
                            selected = uiState.currentTab == NoteDetailTab.TRANSCRIPTION,
                            onClick = { viewModel.updateTab(NoteDetailTab.TRANSCRIPTION) },
                            text = { Text("Transcription") }
                        )
                        Tab(
                            selected = uiState.currentTab == NoteDetailTab.SUMMARY,
                            onClick = { viewModel.updateTab(NoteDetailTab.SUMMARY) },
                            text = { Text("Summary") }
                        )
                    }

                    NoteSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = viewModel::searchInNote,
                        onClearSearch = viewModel::clearSearch,
                        timeFilter = uiState.timeFilter,
                        onTimeFilterChange = viewModel::updateTimeFilter
                    )

                    when (uiState.currentTab) {
                        NoteDetailTab.TRANSCRIPTION -> NoteTranscriptionContent(
                            note = uiState.note!!,
                            searchQuery = uiState.searchQuery,
                            isPlaying = uiState.isPlaying,
                            audioProgress = uiState.audioProgress,
                            audioDuration = uiState.audioDuration,
                            onPlayPause = viewModel::toggleAudioPlayback,
                            onSeek = viewModel::seekAudio,
                            onContentChange = viewModel::updateNote
                        )
                        NoteDetailTab.SUMMARY -> NoteSummaryContent(
                            note = uiState.note!!,
                            isGenerating = uiState.isGeneratingSummary,
                            error = uiState.summaryError
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search in note") },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Clear, "Clear search")
                    }
                }
            }
        )
        IconButton(onClick = { onSearch(query) }) {
            Icon(Icons.Default.Search, "Search")
        }
    }
}

@Composable
private fun NoteTranscriptionContent(
    note: Note,
    searchQuery: String,
    isPlaying: Boolean,
    audioProgress: Float,
    audioDuration: String,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (note.hasAudio) {
            AudioPlayer(
                audioPath = note.audioPath!!,
                isPlaying = isPlaying,
                progress = audioProgress,
                duration = audioDuration,
                onPlayPause = onPlayPause,
                onSeek = onSeek
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = note.content,
            onValueChange = onContentChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun NoteSummaryContent(
    note: Note,
    isGenerating: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            note.summary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } ?: Text("No summary available")
        }
    }
} 