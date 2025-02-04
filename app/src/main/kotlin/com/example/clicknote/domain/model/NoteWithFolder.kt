package com.example.clicknote.domain.model

/**
 * Domain model representing a note with its associated folder
 */
data class NoteWithFolder(
    val note: Note,
    val folder: Folder?
) 