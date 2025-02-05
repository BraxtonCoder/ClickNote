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
    val modifiedAt: LocalDateTime,
    val deletedAt: LocalDateTime? = null,
    val folderId: String? = null,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val isLongForm: Boolean = false,
    val hasAudio: Boolean = false,
    val audioPath: String? = null,
    val duration: Long = 0L,
    val transcriptionLanguage: String = "en",
    val summary: String? = null,
    val keyPoints: List<String> = emptyList(),
    val speakers: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val userId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val source: NoteSource = NoteSource.MANUAL
) {
    val isShortForm: Boolean
        get() = !isLongForm

    companion object {
        fun createEmpty() = Note(
            id = UUID.randomUUID().toString(),
            title = "",
            content = "",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now()
        )

        fun create(
            title: String,
            content: String,
            isLongForm: Boolean = false,
            audioPath: String? = null,
            duration: Long = 0L,
            source: NoteSource = NoteSource.MANUAL,
            folderId: String? = null,
            speakers: Map<String, String> = emptyMap(),
            keyPoints: List<String> = emptyList()
        ): Note {
            val now = LocalDateTime.now()
            return Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                createdAt = now,
                modifiedAt = now,
                isLongForm = isLongForm,
                hasAudio = audioPath != null,
                audioPath = audioPath,
                duration = duration,
                source = source,
                folderId = folderId,
                speakers = speakers,
                keyPoints = keyPoints
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

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
} 