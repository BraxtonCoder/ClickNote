package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import java.time.LocalDateTime

fun NoteEntity.toNote(): Note = Note(
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
    speakers = speakers
)

fun NoteWithFolderEntity.toNote(): Note = note.toNote().copy(
    folderId = folder?.id
)

fun Note.toNoteEntity(): NoteEntity = NoteEntity(
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
    syncStatus = 0 // Default to pending sync
)

fun createNote(
    content: String,
    title: String = "",
    folderId: String? = null,
    hasAudio: Boolean = false,
    audioPath: String? = null,
    source: NoteSource = NoteSource.VOICE
): Note {
    val now = LocalDateTime.now()
    return Note(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        isDeleted = false,
        isPinned = false,
        hasAudio = hasAudio,
        audioPath = audioPath,
        source = source.toString(),
        folderId = folderId,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList()
    )
} 