package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

enum class StorageLocation(val displayName: String, val description: String) {
    LOCAL("Local Storage", "Store notes on device only"),
    GOOGLE_DRIVE("Google Drive", "Sync with Google Drive"),
    AWS("AWS Cloud", "Store in AWS cloud"),
    AZURE("Microsoft Azure", "Store in Azure cloud")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageLocationDialog(
    currentLocation: StorageLocation,
    onLocationSelected: (StorageLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf(currentLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage Location") },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageLocation.values().forEach { location ->
                    ListItem(
                        headlineContent = { Text(location.displayName) },
                        supportingContent = { Text(location.description) },
                        leadingContent = {
                            Icon(
                                when (location) {
                                    StorageLocation.LOCAL -> Icons.Default.Storage
                                    StorageLocation.GOOGLE_DRIVE -> Icons.Default.CloudQueue
                                    StorageLocation.AWS -> Icons.Default.Cloud
                                    StorageLocation.AZURE -> Icons.Default.CloudSync
                                },
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = location == selectedLocation,
                                onClick = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = location == selectedLocation,
                                onClick = { selectedLocation = location },
                                role = Role.RadioButton
                            )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onLocationSelected(selectedLocation)
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 