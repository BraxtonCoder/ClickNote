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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import com.example.clicknote.domain.model.CloudProvider

@Composable
fun StorageLocationDialog(
    selectedProvider: CloudProvider,
    onProviderSelected: (CloudProvider) -> Unit,
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
                    provider = CloudProvider.NONE,
                    title = stringResource(R.string.local_storage),
                    description = stringResource(R.string.store_locally),
                    icon = Icons.Default.Storage,
                    isSelected = selectedProvider == CloudProvider.NONE,
                    onSelect = onProviderSelected
                )
                
                StorageOption(
                    provider = CloudProvider.GOOGLE_CLOUD,
                    title = stringResource(R.string.google_cloud),
                    description = stringResource(R.string.store_in_google_cloud),
                    icon = Icons.Default.Cloud,
                    isSelected = selectedProvider == CloudProvider.GOOGLE_CLOUD,
                    onSelect = onProviderSelected
                )
                
                StorageOption(
                    provider = CloudProvider.LOCAL_CLOUD,
                    title = stringResource(R.string.local_cloud),
                    description = stringResource(R.string.store_in_local_cloud),
                    icon = Icons.Default.CloudOff,
                    isSelected = selectedProvider == CloudProvider.LOCAL_CLOUD,
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
    provider: CloudProvider,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: (CloudProvider) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(provider) }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
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
    }
} 