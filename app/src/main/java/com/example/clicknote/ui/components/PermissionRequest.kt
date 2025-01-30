package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.clicknote.R

@Composable
fun PermissionRequest(
    permissions: List<String>,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.permissions_required),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        permissions.forEach { permission ->
            PermissionItem(permission = permission)
        }

        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.grant_permissions))
        }
    }
}

@Composable
private fun PermissionItem(
    permission: String,
    modifier: Modifier = Modifier
) {
    val (icon, description) = when (permission) {
        android.Manifest.permission.RECORD_AUDIO -> Icons.Default.Mic to stringResource(R.string.permission_microphone)
        android.Manifest.permission.POST_NOTIFICATIONS -> Icons.Default.Notifications to stringResource(R.string.permission_notifications)
        android.Manifest.permission.READ_PHONE_STATE -> Icons.Default.Phone to stringResource(R.string.permission_phone)
        else -> return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
} 