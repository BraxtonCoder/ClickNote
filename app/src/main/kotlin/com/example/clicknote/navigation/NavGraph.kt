package com.example.clicknote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.ui.screens.Onboarding
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Notes : Screen("notes")
    object Settings : Screen("settings")
    object PhoneAuth : Screen("phone_auth")
    object Onboarding : Screen("onboarding")
    object RecycleBin : Screen("recycle_bin")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    userPreferences: UserPreferencesDataStore
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            Onboarding(navController = navController, userPreferences = userPreferences)
        }
        
        composable(Screen.Notes.route) {
            // Notes screen implementation
        }
        
        composable(Screen.Settings.route) {
            // Settings screen implementation
        }
        
        composable(Screen.PhoneAuth.route) {
            // Phone auth screen implementation
        }
        
        composable(Screen.RecycleBin.route) {
            // Recycle bin screen implementation
        }
    }
} 