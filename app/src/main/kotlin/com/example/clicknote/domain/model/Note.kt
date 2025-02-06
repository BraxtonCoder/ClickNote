package com.example.clicknote.domain.model

import java.time.LocalDateTime

/**
 * Represents a note in the application
 */
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val source: NoteSource = NoteSource.MANUAL,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val folderId: String? = null,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val hasAudio: Boolean = false,
    val audioPath: String? = null,
    val duration: Int? = null,
    val transcriptionLanguage: String? = null,
    val speakerCount: Int? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val isLongNote: Boolean
        get() = duration?.let { it >= 30 } ?: false

    val isQuickNote: Boolean
        get() = duration?.let { it < 30 } ?: true

    companion object {
        const val MAX_TITLE_LENGTH = 100
        const val DEFAULT_LANGUAGE = "en"
    }
}