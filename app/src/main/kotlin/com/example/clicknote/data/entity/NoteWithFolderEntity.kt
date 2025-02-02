package com.example.clicknote.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.clicknote.domain.model.Note

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
    fun toNote(): Note = note.toNote().copy(
        folderId = folder?.id
    )

    companion object {
        fun fromNote(note: Note, folder: FolderEntity? = null): NoteWithFolderEntity {
            return NoteWithFolderEntity(
                note = note.toNoteEntity(),
                folder = folder
            )
        }
    }
} 