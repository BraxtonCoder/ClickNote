package com.example.clicknote.navigation

sealed class Screen(val route: String) {
    object Notes : Screen("notes")
    object Settings : Screen("settings")
    object PhoneAuth : Screen("phone_auth")
    object Onboarding : Screen("onboarding")
    object RecycleBin : Screen("recycle_bin")
} 