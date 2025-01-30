package com.example.clicknote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.clicknote.ui.folders.FolderNotesScreen
import com.example.clicknote.ui.note.NoteDetailScreen
import com.example.clicknote.ui.notes.NotesScreen
import com.example.clicknote.ui.recording.RecordingScreen
import com.example.clicknote.ui.settings.SettingsScreen
import com.example.clicknote.ui.bin.RecycleBinScreen

@Composable
fun ClickNoteNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Notes.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateToNote = { noteId -> 
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onNavigateToFolder = { folderId ->
                    navController.navigate(Screen.FolderNotes.createRoute(folderId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToRecycleBin = {
                    navController.navigate(Screen.RecycleBin.route)
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
            RecycleBinScreen(
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Recording.route) {
            RecordingScreen(
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.FolderNotes.route,
            arguments = listOf(
                navArgument("folderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            requireNotNull(folderId) { "folderId parameter wasn't found. Please make sure it's passed." }
            FolderNotesScreen(
                folderId = folderId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNote = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            requireNotNull(noteId) { "noteId parameter wasn't found. Please make sure it's passed." }
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 