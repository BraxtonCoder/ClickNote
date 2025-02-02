package com.example.clicknote.ui.navigation

sealed class Screen(val route: String) {
    data object Notes : Screen("notes")
    data object CreateNote : Screen("create_note")
    data object Settings : Screen("settings")
    data object RecycleBin : Screen("recycle_bin")
    data object Recording : Screen("recording")
    data object InternalAudioCapture : Screen("internal_audio_capture")
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")

    data class NoteDetail(val noteId: String? = null) : Screen(
        route = if (noteId != null) "note_detail/$noteId" else "note_detail/{noteId}"
    ) {
        companion object {
            const val noteIdArg = "noteId"
            val route = "note_detail/{$noteIdArg}"
        }
    }

    data class FolderNotes(val folderId: String? = null) : Screen(
        route = if (folderId != null) "folder_notes/$folderId" else "folder_notes/{folderId}"
    ) {
        companion object {
            const val folderIdArg = "folderId"
            val route = "folder_notes/{$folderIdArg}"
        }
    }

    fun createRoute(vararg args: String): String {
        return when (this) {
            is NoteDetail -> "note_detail/${args.firstOrNull() ?: "{$noteIdArg}"}"
            is FolderNotes -> "folder_notes/${args.firstOrNull() ?: "{$folderIdArg}"}"
            else -> route
        }
    }
} 