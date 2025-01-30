package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiNoteActions(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onCopySelected: () -> String,
    onMoveToFolder: () -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear selection")
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Copy") },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(onCopySelected()))
                        showMenu = false
                        onClearSelection()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Move to Folder") },
                    onClick = {
                        onMoveToFolder()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDeleteSelected()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                )
            }
        },
        modifier = modifier
    )
} 