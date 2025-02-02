package com.example.clicknote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectionDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onFolderSelect: (Folder?) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to Folder") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option to remove from folder
                ListItem(
                    headlineContent = { Text("No Folder") },
                    leadingContent = {
                        Icon(
                            Icons.Default.FolderOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable {
                        onFolderSelect(null)
                        onDismiss()
                    }
                )

                Divider()

                if (folders.isEmpty()) {
                    ListItem(
                        headlineContent = {
                            Text(
                                "No folders created yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                } else {
                    LazyColumn {
                        items(folders) { folder ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        folder.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(folder.color))
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onFolderSelect(folder)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 