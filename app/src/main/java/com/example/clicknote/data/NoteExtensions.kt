package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import java.io.File
import java.time.LocalDateTime

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        content = content,
        timestamp = timestamp,
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath?.let { File(it) },
        isPinned = isPinned,
        isInTrash = isInTrash,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        transcriptionSegments = transcriptionSegments
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        timestamp = timestamp,
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath?.absolutePath,
        isPinned = isPinned,
        isInTrash = isInTrash,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        transcriptionSegments = transcriptionSegments
    )
}

fun Note.copy(
    id: String = this.id,
    content: String = this.content,
    timestamp: LocalDateTime = this.timestamp,
    folderId: String? = this.folderId,
    hasAudio: Boolean = this.hasAudio,
    audioPath: File? = this.audioPath,
    isPinned: Boolean = this.isPinned,
    isInTrash: Boolean = this.isInTrash,
    summary: String? = this.summary,
    keyPoints: List<String> = this.keyPoints,
    speakers: List<String> = this.speakers,
    transcriptionSegments: List<TranscriptionSegment> = this.transcriptionSegments
): Note = Note(
    id = id,
    content = content,
    timestamp = timestamp,
    folderId = folderId,
    hasAudio = hasAudio,
    audioPath = audioPath,
    isPinned = isPinned,
    isInTrash = isInTrash,
    summary = summary,
    keyPoints = keyPoints,
    speakers = speakers,
    transcriptionSegments = transcriptionSegments
)

fun NoteEntity.Companion.create(
    content: String,
    folderId: String? = null,
    hasAudio: Boolean = false,
    audioPath: String? = null,
    source: NoteSource = NoteSource.VOICE
): NoteEntity {
    val now = LocalDateTime.now()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        content = content,
        timestamp = now,
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath?.let { File(it) },
        isPinned = false,
        isInTrash = false,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList(),
        transcriptionSegments = emptyList()
    )
}

fun NoteWithFolder.toNote(): Note = note.toNote().copy(
    folderId = folder?.id
) 