package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class VoiceProfile(
    val id: String,
    val name: String,
    val sampleCount: Int,
    val lastUsed: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceProfileDialog(
    profiles: List<VoiceProfile>,
    onDismiss: () -> Unit,
    onDeleteProfile: (String) -> Unit,
    onRenameProfile: (String, String) -> Unit,
    onCreateProfile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Voice Profiles") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add New Profile Button
                OutlinedButton(
                    onClick = onCreateProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Profile")
                }

                // Profile List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles) { profile ->
                        var showRenameDialog by remember { mutableStateOf(false) }
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        if (showRenameDialog) {
                            var newName by remember { mutableStateOf(profile.name) }
                            AlertDialog(
                                onDismissRequest = { showRenameDialog = false },
                                title = { Text("Rename Profile") },
                                text = {
                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = { Text("Profile Name") }
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onRenameProfile(profile.id, newName)
                                            showRenameDialog = false
                                        }
                                    ) {
                                        Text("Save")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showRenameDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Profile") },
                                text = { Text("Are you sure you want to delete this voice profile?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteProfile(profile.id)
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        ListItem(
                            headlineContent = { Text(profile.name) },
                            supportingContent = { 
                                Text("${profile.sampleCount} samples • Last used: ${profile.lastUsed}")
                            },
                            leadingContent = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { showRenameDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                                    }
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
} 