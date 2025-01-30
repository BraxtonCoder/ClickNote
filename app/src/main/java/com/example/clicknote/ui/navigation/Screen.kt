package com.example.clicknote.ui.navigation

sealed class Screen(val route: String) {
    data object Notes : Screen("notes")
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    data object Recording : Screen("recording")
    data object InternalAudioCapture : Screen("internal_audio_capture")
    data object Settings : Screen("settings")
    data object FolderNotes : Screen("folder_notes/{folderId}") {
        fun createRoute(folderId: String) = "folder_notes/$folderId"
    }
    data object RecycleBin : Screen("recycle_bin")
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Subscription : Screen("subscription")
    data object Onboarding : Screen("onboarding")
} 