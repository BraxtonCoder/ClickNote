package com.example.clicknote.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.components.NotesList
import com.example.clicknote.ui.viewmodel.RecycleBinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecycleBinViewModel = hiltViewModel()
) {
    val notes by viewModel.deletedNotes.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSelectionToolbar by remember { mutableStateOf(false) }

    LaunchedEffect(selectedNotes) {
        showSelectionToolbar = selectedNotes.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showSelectionToolbar) "${selectedNotes.size} selected" else "Recycle Bin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    if (showSelectionToolbar) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                        IconButton(onClick = { viewModel.restoreSelectedNotes() }) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore selected")
                        }
                        IconButton(onClick = { viewModel.deleteSelectedNotesPermanently() }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete permanently")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NotesList(
            notes = notes,
            selectedNotes = selectedNotes,
            isLoading = isLoading,
            onNoteClick = { note ->
                if (showSelectionToolbar) {
                    viewModel.toggleNoteSelection(note.id)
                }
            },
            onNoteLongClick = { note ->
                viewModel.toggleNoteSelection(note.id)
            },
            onPinClick = { /* Disabled in recycle bin */ },
            onDeleteNote = { note ->
                viewModel.deleteNotePermanently(note.id)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
} 