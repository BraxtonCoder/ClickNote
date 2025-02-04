package com.example.clicknote.domain.model

import com.example.clicknote.data.model.SyncStatus
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
    val isLongForm: Boolean = false,
    val hasAudio: Boolean = false,
    val audioPath: String? = null,
    val duration: Long = 0,
    val source: NoteSource = NoteSource.MANUAL,
    val folderId: String? = null,
    val summary: String? = null,
    val keyPoints: List<String> = emptyList(),
    val speakers: List<String> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    val isShortForm: Boolean
        get() = !isLongForm

    companion object {
        fun create(
            title: String,
            content: String,
            isLongForm: Boolean = false,
            audioPath: String? = null,
            duration: Long = 0L,
            source: NoteSource = NoteSource.MANUAL,
            folderId: String? = null
        ): Note {
            val now = LocalDateTime.now()
            return Note(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                content = content,
                createdAt = now,
                updatedAt = now,
                isLongForm = isLongForm,
                hasAudio = audioPath != null,
                audioPath = audioPath,
                duration = duration,
                source = source,
                folderId = folderId
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