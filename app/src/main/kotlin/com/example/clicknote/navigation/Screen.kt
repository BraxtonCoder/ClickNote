package com.example.clicknote.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Notes : Screen("notes")
    object Settings : Screen("settings")
    object PhoneAuth : Screen("phone_auth")
    object Onboarding : Screen("onboarding")
    object RecycleBin : Screen("recycle_bin")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    object FolderDetail : Screen("folder_detail/{folderId}") {
        fun createRoute(folderId: String) = "folder_detail/$folderId"
    }
    object Search : Screen("search")
    object Premium : Screen("premium")
    object Profile : Screen("profile")
    
    companion object {
        fun fromRoute(route: String): Screen {
            return when (route) {
                "home" -> Home
                "notes" -> Notes
                "settings" -> Settings
                "phone_auth" -> PhoneAuth
                "onboarding" -> Onboarding
                "recycle_bin" -> RecycleBin
                "search" -> Search
                "premium" -> Premium
                "profile" -> Profile
                else -> {
                    when {
                        route.startsWith("note_detail/") -> NoteDetail
                        route.startsWith("folder_detail/") -> FolderDetail
                        else -> Home
                    }
                }
            }
        }
    }
} 