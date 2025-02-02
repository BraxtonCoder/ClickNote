package com.example.clicknote.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clicknote.presentation.notes.NotesScreen
import com.example.clicknote.presentation.settings.SettingsScreen
import com.example.clicknote.presentation.subscription.SubscriptionScreen

sealed class Screen(val route: String) {
    object Notes : Screen("notes")
    object Settings : Screen("settings")
    object Subscription : Screen("subscription")
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
        composable(Screen.Notes.route) {
            NotesScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 
