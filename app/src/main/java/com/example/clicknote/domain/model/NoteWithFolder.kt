package com.example.clicknote.domain.model

data class NoteWithFolder(
    val note: Note,
    val folder: Folder?
) 