package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.data.mapper.toDomain
import com.example.clicknote.data.mapper.toEntity

/**
 * Converts a NoteWithFolderEntity to a domain NoteWithFolder model
 */
fun NoteWithFolderEntity.toDomain(): NoteWithFolder {
    return NoteWithFolder(
        note = note.toDomain(),
        folder = folder?.toDomain()
    )
}

/**
 * Converts a domain NoteWithFolder model to a NoteWithFolderEntity
 */
fun NoteWithFolder.toEntity(): NoteWithFolderEntity {
    return NoteWithFolderEntity(
        note = note.toEntity(),
        folder = folder?.toEntity()
    )
} 