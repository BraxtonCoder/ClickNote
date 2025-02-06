package com.example.clicknote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.ui.screens.Onboarding
import javax.inject.Inject

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    userPreferences: UserPreferencesDataStore
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            // Home screen composable
        }
        
        composable(Screen.Notes.route) {
            // Notes screen composable
        }
        
        composable(Screen.Settings.route) {
            // Settings screen composable
        }
        
        composable(Screen.PhoneAuth.route) {
            // Phone auth screen composable
        }
        
        composable(Screen.Onboarding.route) {
            Onboarding(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        
        composable(Screen.RecycleBin.route) {
            // Recycle bin screen composable
        }
        
        composable(Screen.NoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            // Note detail screen composable
        }
        
        composable(Screen.FolderDetail.route) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            // Folder detail screen composable
        }
        
        composable(Screen.Search.route) {
            // Search screen composable
        }
        
        composable(Screen.Premium.route) {
            // Premium screen composable
        }
        
        composable(Screen.Profile.route) {
            // Profile screen composable
        }
    }
} 