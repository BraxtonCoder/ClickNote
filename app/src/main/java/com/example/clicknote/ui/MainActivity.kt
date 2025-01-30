package com.example.clicknote.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.clicknote.ui.components.drawer.NavigationDrawerContent
import com.example.clicknote.ui.navigation.Navigation
import com.example.clicknote.ui.theme.ClickNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()

            ClickNoteTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        NavigationDrawerContent(
                            onFolderClick = { folderId ->
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate(Route.FolderNotes.createRoute(folderId))
                                }
                            },
                            onRecycleBinClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate(Route.RecycleBin.route)
                                }
                            }
                        )
                    }
                ) {
                    Navigation(
                        navController = navController
                    )
                }
            }
        }
    }
} 