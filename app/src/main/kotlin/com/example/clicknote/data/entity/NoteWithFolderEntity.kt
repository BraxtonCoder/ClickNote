package com.example.clicknote.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.clicknote.data.toDomain
import com.example.clicknote.data.toEntity
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
                note = domain.note.toEntity(),
                folder = domain.folder?.toEntity()
            )
        }
    }
} 