package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import java.time.LocalDateTime
import java.time.ZoneOffset

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath,
        isPinned = isPinned,
        isInTrash = isInTrash,
        deletedAt = deletedAt,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        source = source,
        transcriptionSegments = transcriptionSegments
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath,
        isPinned = isPinned,
        isInTrash = isInTrash,
        deletedAt = deletedAt,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        source = source,
        transcriptionSegments = transcriptionSegments
    )
}

fun createNote(
    content: String,
    title: String = "",
    folderId: String? = null,
    hasAudio: Boolean = false,
    audioPath: String? = null,
    source: NoteSource = NoteSource.VOICE
): Note {
    return Note(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        timestamp = System.currentTimeMillis(),
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath,
        isPinned = false,
        isInTrash = false,
        deletedAt = null,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList(),
        source = source,
        transcriptionSegments = emptyList()
    )
} 