package com.example.clicknote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.clicknote.ui.screens.*
import com.example.clicknote.ui.calls.CallRecordingsScreen
import com.example.clicknote.ui.notes.NotesScreen
import com.example.clicknote.ui.settings.SettingsScreen
import com.example.clicknote.ui.recording.InternalAudioCaptureScreen

sealed class Screen(val route: String) {
    data object Notes : Screen("notes")
    data object CreateNote : Screen("create_note")
    data object RecycleBin : Screen("recycle_bin")
    data object NoteDetail : Screen("note_detail")
    data object Recording : Screen("recording")
    data object Settings : Screen("settings")
    data object FolderNotes : Screen("folder_notes")
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Subscription : Screen("subscription")
    data object CallRecordings : Screen("call_recordings")
    data object Onboarding : Screen("onboarding")
    data object InternalAudioCapture : Screen("internal_audio_capture")
    
    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Notes.route -> Notes
                CreateNote.route -> CreateNote
                RecycleBin.route -> RecycleBin
                NoteDetail.route -> NoteDetail
                Recording.route -> Recording
                Settings.route -> Settings
                FolderNotes.route -> FolderNotes
                SignIn.route -> SignIn
                SignUp.route -> SignUp
                Subscription.route -> Subscription
                CallRecordings.route -> CallRecordings
                Onboarding.route -> Onboarding
                InternalAudioCapture.route -> InternalAudioCapture
                else -> Notes
            }
        }

        fun createRoute(noteId: String): String {
            return "${NoteDetail.route}?noteId=$noteId"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Notes.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main notes screen
        composable(Screen.Notes.route) {
            NotesScreen(navController = navController)
        }

        // Note detail screen
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            requireNotNull(noteId) { "Note ID is required" }
            NoteDetailScreen(
                noteId = noteId,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // Recording screen
        composable(Screen.Recording.route) {
            RecordingScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToSubscription = {
                    navController.navigate(Screen.Subscription.route)
                }
            )
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Folder notes screen
        composable(
            route = Screen.FolderNotes.route,
            arguments = listOf(
                navArgument("folderId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            requireNotNull(folderId) { "Folder ID is required" }
            FolderNotesScreen(
                folderId = folderId,
                onNavigateUp = { navController.navigateUp() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        // Recycle bin screen
        composable(Screen.RecycleBin.route) {
            RecycleBinScreen(
                onNavigateUp = { navController.navigateUp() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        // Onboarding screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Sign in screen
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    // TODO: Add forgot password navigation
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route)
                }
            )
        }

        // Subscription screen
        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Call recordings screen
        composable(Screen.CallRecordings.route) {
            CallRecordingsScreen(navController = navController)
        }

        // Internal audio capture screen
        composable(Screen.InternalAudioCapture.route) {
            InternalAudioCaptureScreen()
        }
    }
} 