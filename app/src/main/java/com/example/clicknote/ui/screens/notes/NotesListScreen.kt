package com.example.clicknote.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.clicknote.domain.model.Note
import com.example.clicknote.ui.components.notes.NoteItem
import com.example.clicknote.ui.components.search.SearchBar
import com.example.clicknote.ui.components.search.DateRangeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onFolderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val isSelectionMode = selectedNotes.isNotEmpty()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedNotes.size,
                    onCloseSelection = { viewModel.clearSelection() },
                    onCopySelected = { viewModel.copySelectedNotes() },
                    onDeleteSelected = { viewModel.moveSelectedNotesToTrash() }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { /* Open drawer - handled by parent */ }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewNoteClick,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_note))
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onFilterClick = { viewModel.showDateRangeFilter() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            AnimatedVisibility(visible = viewModel.isDateRangeFilterVisible) {
                DateRangeFilter(
                    onDateRangeSelected = viewModel::setDateRange,
                    onDismiss = { viewModel.hideDateRangeFilter() }
                )
            }

            if (notes.isEmpty()) {
                EmptyNotesPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notes,
                        key = { it.id }
                    ) { note ->
                        NoteItem(
                            note = note,
                            isSelected = note.id in selectedNotes,
                            onClick = { 
                                if (isSelectionMode) {
                                    viewModel.toggleNoteSelection(note.id)
                                } else {
                                    onNoteClick(note.id)
                                }
                            },
                            onLongClick = { viewModel.toggleNoteSelection(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onCopySelected: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onCloseSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onCopySelected) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    )
}

@Composable
private fun EmptyNotesPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_notes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 