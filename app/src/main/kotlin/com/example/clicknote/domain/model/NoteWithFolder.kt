package com.example.clicknote.domain.model

/**
 * Represents a note with its associated folder
 */
data class NoteWithFolder(
    val note: Note,
    val folder: Folder?
) 