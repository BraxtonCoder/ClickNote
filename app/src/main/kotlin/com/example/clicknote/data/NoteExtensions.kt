package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.util.DateTimeUtils
import java.time.LocalDateTime

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = deletedAt,
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

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = deletedAt,
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

fun createNote(
    title: String,
    content: String,
    isLongForm: Boolean = false,
    audioPath: String? = null,
    duration: Long = 0L,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null
): NoteEntity {
    val now = LocalDateTime.now()
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

fun NoteWithFolderEntity.toNote(): Note = note.toDomain().copy(
    folderId = folder?.id
)

fun NoteEntity.Companion.create(
    title: String,
    content: String,
    summary: String? = null,
    keyPoints: List<String> = emptyList(),
    speakers: List<String> = emptyList(),
    audioPath: String? = null,
    duration: Long = 0L,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    isPinned: Boolean = false,
    isLongForm: Boolean = false
): NoteEntity {
    val now = LocalDateTime.now()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        modifiedAt = now,
        deletedAt = null,
        isDeleted = false,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = SyncStatus.PENDING.name
    )
} 