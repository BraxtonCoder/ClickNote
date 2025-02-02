package com.example.clicknote.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.clicknote.ui.components.NavigationDrawerContent
import com.example.clicknote.ui.components.MainTopBar
import com.example.clicknote.ui.components.RecordingFab
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                folders = uiState.folders,
                selectedFolderId = uiState.selectedFolderId,
                onFolderClick = { folderId -> viewModel.selectFolder(folderId) },
                onCreateFolder = { viewModel.showCreateFolderDialog() },
                onFolderOptions = { folder -> viewModel.showFolderOptionsDialog(folder) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                MainTopBar(
                    drawerState = drawerState,
                    scope = scope
                )
            },
            floatingActionButton = {
                RecordingFab(navController)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                // Navigation content will be added here
            }
        }
    }
} 