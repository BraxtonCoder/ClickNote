package com.example.clicknote.ui.folders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.ui.components.NoteItem
import com.example.clicknote.ui.components.SearchBar
import com.example.clicknote.ui.notes.TimeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderNotesScreen(
    folderId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (String) -> Unit,
    viewModel: FolderNotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(folderId) {
        viewModel.loadNotes(folderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.folder?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
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
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                timeFilter = uiState.timeFilter,
                onSearch = viewModel::searchNotes,
                onClearSearch = viewModel::clearSearch,
                onTimeFilterChange = viewModel::updateTimeFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_notes_in_folder),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.notes,
                        key = { it.note.id }
                    ) { noteWithFolder ->
                        NoteItem(
                            note = noteWithFolder.note,
                            onClick = { onNavigateToNote(noteWithFolder.note.id) },
                            onTogglePin = { viewModel.togglePin(noteWithFolder.note.id) },
                            onMoveToTrash = { viewModel.moveToTrash(noteWithFolder.note.id) }
                        )
                    }
                }
            }
        }
    }
} 