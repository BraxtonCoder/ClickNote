package com.example.clicknote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.clicknote.ui.screens.NotesScreen
import com.example.clicknote.ui.screens.SettingsScreen
import com.example.clicknote.ui.screens.note.CreateNoteScreen
import com.example.clicknote.ui.screens.note.NoteDetailScreen
import com.example.clicknote.ui.screens.folder.FolderNotesScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.NoteDetail(noteId).route)
                },
                onNavigateToCreateNote = {
                    navController.navigate(Screen.CreateNote.route)
                }
            )
        }

        composable(Screen.CreateNote.route) {
            CreateNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument(Screen.NoteDetail.noteIdArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(Screen.NoteDetail.noteIdArg)
                ?: return@composable
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.FolderNotes.route,
            arguments = listOf(
                navArgument(Screen.FolderNotes.folderIdArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString(Screen.FolderNotes.folderIdArg)
                ?: return@composable
            FolderNotesScreen(
                folderId = folderId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.NoteDetail(noteId).route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.RecycleBin.route) {
            // TODO: Implement RecycleBin screen
        }
    }
} 