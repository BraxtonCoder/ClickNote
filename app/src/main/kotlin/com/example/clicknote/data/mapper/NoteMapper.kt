package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.util.DateTimeUtils

/**
 * Converts a NoteEntity to a domain Note model
 */
fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.timestampToLocalDateTime(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        source = NoteSource.valueOf(source),
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
}

/**
 * Converts a NoteWithFolderEntity to a domain Note model
 */
fun NoteWithFolderEntity.toNote(): Note = note.toDomain().copy(
    folderId = folder?.id
)

/**
 * Converts a domain Note model to a NoteEntity
 */
fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        modifiedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.localDateTimeToTimestamp(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = syncStatus.name
    )
}

/**
 * Creates a new NoteEntity with default values
 */
fun createNote(
    title: String,
    content: String,
    isLongForm: Boolean = false,
    audioPath: String? = null,
    duration: Long = 0L,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null
): NoteEntity {
    val now = System.currentTimeMillis()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        modifiedAt = now,
        deletedAt = null,
        isDeleted = false,
        isPinned = false,
        isLongForm = isLongForm,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList(),
        syncStatus = SyncStatus.PENDING.name
    )
} 