package com.example.clicknote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clicknote.domain.model.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    selectedFolder: Folder?,
    folders: List<Folder>,
    onFolderClick: (Folder?) -> Unit,
    onAddFolder: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onFolderLongClick: (Folder) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        
        // App Name
        ListItem(
            headlineContent = { 
                Text(
                    "ClickNote",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
        
        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        
        // All Notes
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Notes, contentDescription = null) },
            label = { Text("All Notes") },
            selected = selectedFolder == null,
            onClick = { onFolderClick(null) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Recycle Bin
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            label = { Text("Recycle Bin") },
            selected = false,
            onClick = onRecycleBinClick,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        // Folders Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Folders",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onAddFolder) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Folder",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(folders) { folder ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(folder.color)
                        )
                    },
                    label = {
                        Text(
                            folder.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = selectedFolder?.id == folder.id,
                    onClick = { onFolderClick(folder) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .combinedClickable(
                            onClick = { onFolderClick(folder) },
                            onLongClick = { onFolderLongClick(folder) }
                        )
                )
            }
        }
    }
} 