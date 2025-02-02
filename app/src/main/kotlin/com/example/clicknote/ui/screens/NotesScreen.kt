package com.example.clicknote.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import com.example.clicknote.data.entity.Note
import com.example.clicknote.domain.model.Note as DomainNote
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.ui.components.DateRangeFilterDialog
import com.example.clicknote.ui.components.NavigationDrawer
import com.example.clicknote.ui.components.NoteItem
import com.example.clicknote.ui.viewmodel.NotesViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val spacing = LocalSpacing.current
    var showFilterDialog by remember { mutableStateOf(false) }
    val isSelectionMode = selectedNotes.isNotEmpty()

    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSelectionMode) {
                        Text(stringResource(R.string.app_name))
                    } else {
                        Text("${selectedNotes.size} selected")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        // Selection mode actions
                        IconButton(onClick = { onDeleteNotes(selectedNotes) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(
                            onClick = {
                                if (selectedNotes.size == 1) {
                                    val noteId = selectedNotes.first()
                                    val note = notes.find { it.id == noteId }
                                    if (note?.isPinned == true) {
                                        onUnpinNote(noteId)
                                    } else {
                                        onPinNote(noteId)
                                    }
                                }
                            },
                            enabled = selectedNotes.size == 1
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin/Unpin")
                        }
                    } else {
                        // Normal mode actions
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.action_search)
                    )
                },
                singleLine = true
            )

            // Notes list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.small),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                if (pinnedNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pinned",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(
                                start = spacing.medium,
                                bottom = spacing.small
                            )
                        )
                    }
                    items(
                        items = pinnedNotes,
                        key = { it.id }
                    ) { note ->
                        NoteItem(
                            note = note,
                            isSelected = selectedNotes.contains(note.id),
                            onClick = { onNoteClick(note.id) },
                            onLongClick = { onNoteLongClick(note) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = spacing.medium))
                    }
                }

                if (notes.isNotEmpty()) {
                    items(
                        items = notes.filter { !it.isPinned },
                        key = { it.id }
                    ) { note ->
                        NoteItem(
                            note = note,
                            isSelected = selectedNotes.contains(note.id),
                            onClick = { onNoteClick(note.id) },
                            onLongClick = { onNoteLongClick(note) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.extraLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.empty_notes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (showFilterDialog) {
            DateRangeFilterDialog(
                currentFilter = timeFilter,
                onFilterSelected = { 
                    onTimeFilterChange(it)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
} 