package com.example.clicknote.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note

data class NoteWithFolder(
    @Embedded
    val note: NoteEntity,

    @Relation(
        parentColumn = "folder_id",
        entityColumn = "id"
    )
    val folder: FolderEntity?
) {
    fun toNote(): Note = note.toNote().copy(
        folderId = folder?.id
    )

    companion object {
        fun fromNote(note: Note, folder: FolderEntity? = null): NoteWithFolder {
            return NoteWithFolder(
                note = note.toEntity(),
                folder = folder
            )
        }
    }
} 