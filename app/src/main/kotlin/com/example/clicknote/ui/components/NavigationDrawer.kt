package com.example.clicknote.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.ui.theme.LocalSpacing
import com.example.clicknote.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    currentRoute: String
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(12.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Note, contentDescription = null) },
            label = { Text(stringResource(R.string.notes)) },
            selected = currentRoute == Screen.Notes.route,
            onClick = {
                onNavigate(Screen.Notes)
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Phone, contentDescription = null) },
            label = { Text(stringResource(R.string.call_recordings)) },
            selected = currentRoute == Screen.CallRecordings.route,
            onClick = {
                onNavigate(Screen.CallRecordings)
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.GraphicEq, contentDescription = null) },
            label = { Text(stringResource(R.string.internal_audio)) },
            selected = currentRoute == Screen.InternalAudioCapture.route,
            onClick = {
                onNavigate(Screen.InternalAudioCapture)
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) },
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                onNavigate(Screen.Settings)
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@Composable
fun NavigationDrawer(
    selectedFolder: Folder?,
    folders: List<Folder>,
    onFolderClick: (Folder?) -> Unit,
    onCreateFolder: () -> Unit,
    onFolderLongClick: (Folder) -> Unit,
    onRecycleBinClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(spacing.small)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.medium),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.action_search)
                )
            },
            singleLine = true
        )

        // Navigation Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            item {
                NavigationItem(
                    icon = Icons.Default.Notes,
                    label = stringResource(R.string.nav_notes),
                    selected = selectedFolder == null,
                    onClick = { onFolderClick(null) }
                )
            }

            // Folders Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.nav_folders),
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = onCreateFolder) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.dialog_create_folder_title)
                        )
                    }
                }
            }

            items(folders) { folder ->
                NavigationItem(
                    icon = Icons.Default.Folder,
                    label = folder.name,
                    selected = folder == selectedFolder,
                    onClick = { onFolderClick(folder) },
                    onLongClick = { onFolderLongClick(folder) },
                    iconTint = folder.color
                )
            }

            // Recycle Bin
            item {
                NavigationItem(
                    icon = Icons.Default.Delete,
                    label = stringResource(R.string.nav_recycle_bin),
                    selected = false,
                    onClick = onRecycleBinClick
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    iconTint: Int? = null,
    modifier: Modifier = Modifier
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (onLongClick != null) {
                Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            } else {
                Modifier.clickable(onClick = onClick)
            }),
        color = background,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint?.let { Color(it) } ?: LocalContentColor.current
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ClickNote",
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, "Settings")
        }
    }
} 