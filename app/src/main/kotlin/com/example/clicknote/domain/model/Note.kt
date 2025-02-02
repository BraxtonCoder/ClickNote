package com.example.clicknote.domain.model

import java.io.File
import java.time.LocalDateTime
import java.util.UUID

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val hasAudio: Boolean = false,
    val audioPath: String? = null,
    val source: String = "MANUAL",
    val folderId: String? = null,
    val summary: String? = null,
    val keyPoints: List<String> = emptyList(),
    val speakers: List<String> = emptyList()
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
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                folderId = folderId,
                hasAudio = hasAudio,
                audioPath = audioPath,
                source = source.toString()
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