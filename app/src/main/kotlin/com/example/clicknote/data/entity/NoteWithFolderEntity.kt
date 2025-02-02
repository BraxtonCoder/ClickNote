package com.example.clicknote.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.clicknote.domain.model.NoteWithFolder

data class NoteWithFolderEntity(
    @Embedded
    val note: NoteEntity,

    @Relation(
        parentColumn = "folder_id",
        entityColumn = "id",
        entity = FolderEntity::class
    )
    val folder: FolderEntity?
) {
    fun toDomain() = NoteWithFolder(
        note = note.toDomain(),
        folder = folder?.toDomain()
    )

    companion object {
        fun fromDomain(noteWithFolder: NoteWithFolder): NoteWithFolderEntity {
            val note = NoteEntity.fromDomain(noteWithFolder.note)
            val folder = noteWithFolder.folder?.let { FolderEntity.fromDomain(it) }
            return NoteWithFolderEntity(note, folder)
        }
    }
} 