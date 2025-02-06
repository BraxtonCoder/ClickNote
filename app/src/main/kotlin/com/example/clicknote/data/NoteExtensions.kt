package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import java.time.LocalDateTime

/**
 * Extension function to convert a NoteEntity to a domain Note
 */
fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        source = NoteSource.fromString(source),
        syncStatus = SyncStatus.fromString(syncStatus),
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = isDeleted,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration?.toInt(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Extension function to convert a domain Note to a NoteEntity
 */
fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        source = source.name,
        syncStatus = syncStatus.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = isDeleted,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration?.toLong(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Helper function to create a new note entity
 */
fun createNote(
    title: String,
    content: String,
    audioPath: String? = null,
    duration: Long? = null,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    isPinned: Boolean = false,
    isArchived: Boolean = false
): NoteEntity {
    val now = LocalDateTime.now()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        modifiedAt = now,
        source = source.name,
        syncStatus = SyncStatus.PENDING.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = false,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        transcriptionLanguage = null,
        speakerCount = null,
        metadata = emptyMap()
    )
}

fun NoteWithFolderEntity.toNote(): Note = note.toDomain().copy(
    folderId = folder?.id
)

/**
 * Helper function to create a new note entity with additional options
 */
fun createNoteWithOptions(
    title: String,
    content: String,
    audioPath: String? = null,
    duration: Long? = null,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    isPinned: Boolean = false,
    isArchived: Boolean = false,
    transcriptionLanguage: String? = null,
    speakerCount: Int? = null,
    metadata: Map<String, String> = emptyMap()
): NoteEntity {
    val now = LocalDateTime.now()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        modifiedAt = now,
        source = source.name,
        syncStatus = SyncStatus.PENDING.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = false,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
} 