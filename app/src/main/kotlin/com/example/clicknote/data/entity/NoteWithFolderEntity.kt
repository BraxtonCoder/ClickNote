package com.example.clicknote.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteWithFolder
import com.example.clicknote.data.mapper.toNote
import com.example.clicknote.data.mapper.toNoteEntity

data class NoteWithFolderEntity(
    @Embedded
    val note: NoteEntity,

    @Relation(
        parentColumn = "folder_id",
        entityColumn = "id"
    )
    val folder: FolderEntity?
) {
    fun toDomain(): NoteWithFolder {
        return NoteWithFolder(
            note = note.toDomain(),
            folder = folder?.toDomain()
        )
    }

    companion object {
        fun fromDomain(domain: NoteWithFolder): NoteWithFolderEntity {
            return NoteWithFolderEntity(
                note = NoteEntity.fromDomain(domain.note),
                folder = domain.folder?.let { FolderEntity.fromDomain(it) }
            )
        }
    }
} 