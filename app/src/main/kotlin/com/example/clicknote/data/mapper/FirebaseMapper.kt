package com.example.clicknote.data.mapper

import com.example.clicknote.domain.model.*
import com.example.clicknote.util.DateTimeUtils

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
        syncStatus = syncStatus?.let { SyncStatus.valueOf(it) } ?: SyncStatus.SYNCED
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
        isDeleted = isDeleted
    )
} 