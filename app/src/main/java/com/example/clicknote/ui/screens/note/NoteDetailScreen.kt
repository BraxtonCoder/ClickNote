package com.example.clicknote.ui.screens.note

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.ui.components.audio.AudioPlayer
import com.example.clicknote.ui.components.note.NoteContent
import com.example.clicknote.ui.components.note.NoteSummary
import com.example.clicknote.ui.components.search.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.note_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (uiState.note?.hasAudio == true) {
                        IconButton(
                            onClick = { viewModel.toggleAudioPlayback() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) {
                                    Icons.Default.PauseCircle
                                } else {
                                    Icons.Default.PlayCircle
                                },
                                contentDescription = null
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.togglePin() }
                    ) {
                        Icon(
                            imageVector = if (uiState.note?.isPinned == true) {
                                Icons.Default.PushPin
                            } else {
                                Icons.Default.PushPinOutlined
                            },
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onFilterClick = { /* No filter in note detail */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Content/Summary toggle
            TabRow(
                selectedTabIndex = if (uiState.showingSummary) 1 else 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = !uiState.showingSummary,
                    onClick = { viewModel.setShowingSummary(false) },
                    text = { Text(stringResource(R.string.transcription)) }
                )
                Tab(
                    selected = uiState.showingSummary,
                    onClick = { viewModel.setShowingSummary(true) },
                    text = { Text(stringResource(R.string.summary)) }
                )
            }

            // Audio player
            if (uiState.note?.hasAudio == true) {
                AudioPlayer(
                    isPlaying = uiState.isPlaying,
                    progress = uiState.audioProgress,
                    onProgressChange = viewModel::seekTo,
                    onPlayPause = viewModel::toggleAudioPlayback,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // Main content
            AnimatedContent(
                targetState = uiState.showingSummary,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { showingSummary ->
                if (showingSummary) {
                    NoteSummary(
                        summary = uiState.note?.summary.orEmpty(),
                        keyPoints = uiState.note?.keyPoints.orEmpty(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    NoteContent(
                        content = uiState.note?.content.orEmpty(),
                        searchQuery = uiState.searchQuery,
                        isEditing = uiState.isEditing,
                        onContentChange = viewModel::updateContent,
                        onEditClick = viewModel::toggleEditing,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_note)) },
            text = { Text(stringResource(R.string.delete_note_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.moveToTrash()
                        showDeleteDialog = false
                        onBackClick()
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
} 