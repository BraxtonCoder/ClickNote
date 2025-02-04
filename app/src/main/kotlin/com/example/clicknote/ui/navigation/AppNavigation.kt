package com.example.clicknote.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clicknote.ui.components.NavigationDrawer
import com.example.clicknote.ui.screens.notes.NotesScreen
import com.example.clicknote.ui.screens.settings.SettingsScreen
import com.example.clicknote.ui.screens.folders.FoldersScreen
import com.example.clicknote.ui.screens.trash.TrashScreen

sealed class Screen(val route: String) {
    object Notes : Screen("notes")
    object Settings : Screen("settings")
    object Folders : Screen("folders")
    object Trash : Screen("trash")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route ?: Screen.Notes.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onSignInClick = onSignInClick,
                onSignOutClick = onSignOutClick
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ClickNote") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Notes.route
                ) {
                    composable(Screen.Notes.route) { NotesScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                    composable(Screen.Folders.route) { FoldersScreen() }
                    composable(Screen.Trash.route) { TrashScreen() }
                }
            }
        }
    }
} 