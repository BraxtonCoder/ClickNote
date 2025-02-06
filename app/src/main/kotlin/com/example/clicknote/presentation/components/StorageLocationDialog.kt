package com.example.clicknote.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import com.example.clicknote.domain.model.CloudStorageType

@Composable
fun StorageLocationDialog(
    selectedProvider: CloudStorageType,
    onProviderSelected: (CloudStorageType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.select_storage_location))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageOption(
                    provider = CloudStorageType.NONE,
                    title = stringResource(R.string.local_storage),
                    description = stringResource(R.string.store_locally),
                    icon = Icons.Default.Storage,
                    isSelected = selectedProvider == CloudStorageType.NONE,
                    onSelect = onProviderSelected
                )
                
                StorageOption(
                    provider = CloudStorageType.FIREBASE,
                    title = stringResource(R.string.google_cloud),
                    description = stringResource(R.string.store_in_google_cloud),
                    icon = Icons.Default.Cloud,
                    isSelected = selectedProvider == CloudStorageType.FIREBASE,
                    onSelect = onProviderSelected
                )
                
                StorageOption(
                    provider = CloudStorageType.LOCAL_CLOUD,
                    title = stringResource(R.string.local_cloud),
                    description = stringResource(R.string.store_in_local_cloud),
                    icon = Icons.Default.CloudOff,
                    isSelected = selectedProvider == CloudStorageType.LOCAL_CLOUD,
                    onSelect = onProviderSelected
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.done))
            }
        }
    )
}

@Composable
private fun StorageOption(
    provider: CloudStorageType,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: (CloudStorageType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(provider) }
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
} 