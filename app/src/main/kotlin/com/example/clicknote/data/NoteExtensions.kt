package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.data.model.SyncStatus
import java.time.LocalDateTime
import com.example.clicknote.data.entity.NoteWithFolderEntity

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
        hasAudio = hasAudio,
        audioPath = audioPath,
        source = NoteSource.valueOf(source),
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = syncStatus
    )
}

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
        hasAudio = hasAudio,
        audioPath = audioPath,
        source = source.name,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = syncStatus
    )
}

fun Note.copy(
    id: String = this.id,
    title: String = this.title,
    content: String = this.content,
    createdAt: LocalDateTime = this.createdAt,
    updatedAt: LocalDateTime = this.updatedAt,
    deletedAt: LocalDateTime? = this.deletedAt,
    isDeleted: Boolean = this.isDeleted,
    isPinned: Boolean = this.isPinned,
    hasAudio: Boolean = this.hasAudio,
    audioPath: String? = this.audioPath,
    source: NoteSource = this.source,
    folderId: String? = this.folderId,
    summary: String? = this.summary,
    keyPoints: List<String> = this.keyPoints,
    speakers: List<String> = this.speakers,
    syncStatus: SyncStatus = this.syncStatus
): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    isDeleted = isDeleted,
    isPinned = isPinned,
    hasAudio = hasAudio,
    audioPath = audioPath,
    source = source,
    folderId = folderId,
    summary = summary,
    keyPoints = keyPoints,
    speakers = speakers,
    syncStatus = syncStatus
)

fun createNote(
    title: String,
    content: String,
    folderId: String? = null,
    hasAudio: Boolean = false,
    audioPath: String? = null,
    source: NoteSource = NoteSource.VOICE
): NoteEntity {
    val now = LocalDateTime.now()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
        isPinned = false,
        hasAudio = hasAudio,
        audioPath = audioPath,
        source = source.name,
        folderId = folderId,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList(),
        syncStatus = SyncStatus.PENDING
    )
}

fun NoteWithFolderEntity.toNote(): Note = note.toNote().copy(
    folderId = folder?.id
) 