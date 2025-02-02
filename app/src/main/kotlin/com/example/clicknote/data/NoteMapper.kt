package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        audioPath = audioPath,
        isInTrash = isInTrash,
        isPinned = isPinned,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        source = source,
        deletedAt = deletedAt
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        audioPath = audioPath,
        isInTrash = isInTrash,
        isPinned = isPinned,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        source = source,
        deletedAt = deletedAt
    )
}

fun createNote(
    id: String = java.util.UUID.randomUUID().toString(),
    title: String = "",
    content: String = "",
    audioPath: String? = null,
    source: NoteSource = NoteSource.MANUAL
): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = System.currentTimeMillis(),
        audioPath = audioPath,
        isInTrash = false,
        isPinned = false,
        folderId = null,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList(),
        source = source,
        deletedAt = null
    )
} 