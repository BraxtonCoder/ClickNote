package com.example.clicknote.data.mapper

import com.example.clicknote.domain.model.*
import com.example.clicknote.util.DateTimeUtils
import java.time.LocalDateTime

data class FirebaseNote(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val deletedAt: Long? = null,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val isLongForm: Boolean = false,
    val hasAudio: Boolean = false,
    val audioUrl: String? = null,
    val duration: Long = 0L,
    val source: String = NoteSource.MANUAL.name,
    val folderId: String? = null,
    val summary: String? = null,
    val keyPoints: List<String> = emptyList(),
    val speakers: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val platform: String = "android",
    val syncStatus: String = SyncStatus.PENDING.name
)

data class FirebaseFolder(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val color: Int = 0,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val deletedAt: Long? = null,
    val isDeleted: Boolean = false
)

fun Note.toFirebaseNote(userId: String): FirebaseNote {
    return FirebaseNote(
        id = id,
        userId = userId,
        title = title,
        content = content,
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        modifiedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.localDateTimeToTimestamp(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioUrl = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        tags = tags,
        platform = "android",
        syncStatus = syncStatus.name
    )
}

fun FirebaseNote.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.timestampToLocalDateTime(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioUrl,
        duration = duration,
        source = NoteSource.valueOf(source),
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        tags = tags,
        userId = userId,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
}

fun Folder.toFirebaseFolder(userId: String): FirebaseFolder {
    return FirebaseFolder(
        id = id,
        userId = userId,
        name = name,
        color = color,
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        modifiedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.localDateTimeToTimestamp(it) },
        isDeleted = isDeleted
    )
}

fun FirebaseFolder.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.timestampToLocalDateTime(it) },
        isDeleted = isDeleted,
        sortOrder = 0,
        noteCount = 0,
        userId = userId
    )
} 