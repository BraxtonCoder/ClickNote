package com.example.clicknote.domain.model

import kotlinx.serialization.Serializable
import java.util.*
import java.time.LocalDateTime

/**
 * Represents a note in the application
 */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val summary: String? = null,
    val audioPath: String? = null,
    val createdAt: Long,
    val modifiedAt: Long,
    val folderId: String? = null,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val source: NoteSource = NoteSource.MANUAL,
    val isArchived: Boolean = false,
    val duration: Int? = null,
    val transcriptionLanguage: String? = null,
    val speakerCount: Int? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val isLongNote: Boolean
        get() = duration?.let { it >= 30 } ?: false

    val isQuickNote: Boolean
        get() = duration?.let { it < 30 } ?: true

    val hasAudio: Boolean
        get() = !audioPath.isNullOrEmpty()

    companion object {
        const val MAX_TITLE_LENGTH = 100
        const val DEFAULT_LANGUAGE = "en"
        const val LONG_NOTE_THRESHOLD_SECONDS = 30

        fun create(
            title: String,
            content: String,
            audioPath: String? = null,
            duration: Int? = null,
            source: NoteSource = NoteSource.MANUAL,
            folderId: String? = null
        ): Note {
            val now = System.currentTimeMillis()
            return Note(
                id = java.util.UUID.randomUUID().toString(),
                title = title.take(MAX_TITLE_LENGTH),
                content = content,
                audioPath = audioPath,
                createdAt = now,
                modifiedAt = now,
                folderId = folderId,
                duration = duration,
                source = source,
                transcriptionLanguage = DEFAULT_LANGUAGE
            )
        }
    }
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED,
    CANCELLED,
    CONFLICT,
    OFFLINE
}

data class SyncError(
    val message: String,
    val type: SyncErrorType,
    val noteId: String? = null
)