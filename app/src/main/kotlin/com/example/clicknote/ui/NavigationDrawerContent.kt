package com.example.clicknote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clicknote.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "ClickNote",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Divider()

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Notes, contentDescription = null) },
            label = { Text("Notes") },
            selected = false,
            onClick = { onNavigate(Screen.Notes.route) },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            label = { Text("Recycle Bin") },
            selected = false,
            onClick = { onNavigate(Screen.RecycleBin.route) },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Folders",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Create New Folder",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* TODO: Implement create folder */ }) {
                Icon(Icons.Default.Add, contentDescription = "Create Folder")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = false,
            onClick = { onNavigate(Screen.Settings.route) },
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
} 