package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.data.toEntity
import com.example.clicknote.data.toDomain

/**
 * Converts a domain NoteWithFolder to a NoteWithFolderEntity
 */
fun NoteWithFolder.toEntity(): NoteWithFolderEntity {
    return NoteWithFolderEntity(
        note = note.toEntity(),
        folder = folder?.toEntity()
    )
}

/**
 * Converts a NoteWithFolderEntity to a domain NoteWithFolder
 */
fun NoteWithFolderEntity.toDomain(): NoteWithFolder {
    return NoteWithFolder(
        note = note.toDomain(),
        folder = folder?.toDomain()
    )
} 