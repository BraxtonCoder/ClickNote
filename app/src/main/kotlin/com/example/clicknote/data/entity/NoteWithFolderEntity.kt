package com.example.clicknote.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteWithFolder

/**
 * Room entity representing a note with its associated folder
 */
data class NoteWithFolderEntity(
    @Embedded
    val note: NoteEntity,

    @Relation(
        parentColumn = "folder_id",
        entityColumn = "id"
    )
    val folder: FolderEntity?
) {
    /**
     * Converts this entity to a domain model
     */
    fun toDomain(): NoteWithFolder {
        return NoteWithFolder(
            note = note.toDomain(),
            folder = folder?.toDomain()
        )
    }

    companion object {
        /**
         * Creates an entity from a domain model
         */
        fun fromDomain(domain: NoteWithFolder): NoteWithFolderEntity {
            return NoteWithFolderEntity(
                note = NoteEntity.fromDomain(domain.note),
                folder = domain.folder?.let { FolderEntity.fromDomain(it) }
            )
        }
    }
} 