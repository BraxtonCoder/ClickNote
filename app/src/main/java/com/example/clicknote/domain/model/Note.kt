package com.example.clicknote.domain.model

import java.io.File
import java.time.LocalDateTime
import java.util.UUID

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val audioPath: String? = null,
    val isInTrash: Boolean = false,
    val isPinned: Boolean = false,
    val folderId: String? = null,
    val summary: String? = null,
    val keyPoints: List<String> = emptyList(),
    val speakers: List<String> = emptyList(),
    val source: NoteSource = NoteSource.MANUAL,
    val deletedAt: Long? = null
) {
    companion object {
        fun create(
            title: String,
            content: String,
            folderId: String? = null,
            hasAudio: Boolean = false,
            audioPath: String? = null,
            source: NoteSource = NoteSource.VOICE
        ): Note {
            return Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                folderId = folderId,
                hasAudio = hasAudio,
                audioPath = audioPath,
                source = source
            )
        }
    }
}

enum class NoteSource {
    MANUAL,
    VOICE,
    CALL,
    IMPORT
} 