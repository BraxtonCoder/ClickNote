package com.example.clicknote.ui.screens.notes

import androidx.compose.animation.animateItemPlacement
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.components.NoteItem
import com.example.clicknote.viewmodel.NotesViewModel
import com.example.clicknote.ui.theme.spacing

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val notes by viewModel.notes.collectAsState()
    val pinnedNotes by viewModel.pinnedNotes.collectAsState()
    
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Note"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pinned Notes Section
            if (pinnedNotes.isNotEmpty()) {
                Text(
                    text = "Pinned",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(spacing.medium)
                )
                
                pinnedNotes.forEach { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = spacing.medium))
            }
            
            // Other Notes Section
            if (notes.isNotEmpty()) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(spacing.medium)
                )
                
                notes.forEach { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
} 