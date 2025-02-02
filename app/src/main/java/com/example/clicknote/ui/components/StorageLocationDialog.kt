package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

enum class StorageLocation(val title: String, val description: String) {
    LOCAL("Local Storage", "Store on device"),
    GOOGLE_CLOUD("Google Cloud", "Store in Google Cloud")
}

@Composable
fun StorageLocationDialog(
    selectedLocation: StorageLocation,
    onLocationSelected: (StorageLocation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Storage Location") },
        text = {
            Column {
                StorageLocation.values().forEach { location ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = location == selectedLocation,
                                onClick = { onLocationSelected(location) }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = location == selectedLocation,
                            onClick = { onLocationSelected(location) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = when (location) {
                                StorageLocation.LOCAL -> Icons.Default.Storage
                                StorageLocation.GOOGLE_CLOUD -> Icons.Default.Cloud
                            },
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = location.title)
                            Text(
                                text = location.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
} 