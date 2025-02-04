package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.data.model.SyncStatus
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import java.time.LocalDateTime

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
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

fun NoteWithFolderEntity.toNote(): Note = note.toNote().copy(
    folderId = folder?.id
)

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
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
        updatedAt = now,
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