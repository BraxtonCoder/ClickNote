package com.example.clicknote.ui.components.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
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