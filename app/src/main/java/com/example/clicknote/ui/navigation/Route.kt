package com.example.clicknote.ui.navigation

sealed class Route(val route: String) {
    data object NotesList : Route("notes_list")
    data object NoteDetail : Route("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    data object Settings : Route("settings")
    data object RecycleBin : Route("recycle_bin")
    data object FolderNotes : Route("folder/{folderId}") {
        fun createRoute(folderId: String) = "folder/$folderId"
    }
    data object NewNote : Route("new_note")
    data object Auth : Route("auth")
    data object Subscription : Route("subscription")
} 