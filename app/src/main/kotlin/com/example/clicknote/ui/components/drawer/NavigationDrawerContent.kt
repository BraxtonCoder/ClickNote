package com.example.clicknote.ui.components.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.ui.screens.folder.FolderViewModel

@Composable
fun NavigationDrawerContent(
    onFolderClick: (String) -> Unit,
    onRecycleBinClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val folders by viewModel.folders.collectAsState(initial = emptyList())
    var showNewFolderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Folders section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.folders),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { showNewFolderDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_folder)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(folders) { folder ->
                FolderItem(
                    folder = folder,
                    onClick = { onFolderClick(folder.id) }
                )
            }
        }

        // Recycle Bin
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            label = { Text(stringResource(R.string.recycle_bin)) },
            selected = false,
            onClick = onRecycleBinClick,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { name, color ->
                viewModel.createFolder(name, color)
                showNewFolderDialog = false
            }
        )
    }
}

@Composable
private fun FolderItem(
    folder: Folder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
        label = { Text(folder.name) },
        selected = false,
        onClick = onClick,
        modifier = modifier.padding(vertical = 4.dp)
    )
} 