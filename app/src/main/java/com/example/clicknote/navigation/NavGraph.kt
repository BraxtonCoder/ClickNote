package com.example.clicknote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clicknote.ui.auth.PhoneAuthScreen
import com.example.clicknote.ui.home.HomeScreen
import com.example.clicknote.ui.notes.NotesScreen
import com.example.clicknote.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Notes : Screen("notes")
    object Settings : Screen("settings")
    object PhoneAuth : Screen("phone_auth")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPhoneAuth = { navController.navigate(Screen.PhoneAuth.route) }
            )
        }

        composable(Screen.PhoneAuth.route) {
            PhoneAuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PhoneAuth.route) { inclusive = true }
                    }
                }
            )
        }
    }
} 