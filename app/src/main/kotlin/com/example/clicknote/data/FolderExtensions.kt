package com.example.clicknote.data

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = timestampToLocalDateTime(createdAt),
        modifiedAt = timestampToLocalDateTime(modifiedAt),
        deletedAt = deletedAt?.let { timestampToLocalDateTime(it) },
        isDeleted = isDeleted
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = localDateTimeToTimestamp(createdAt),
        modifiedAt = localDateTimeToTimestamp(modifiedAt),
        deletedAt = deletedAt?.let { localDateTimeToTimestamp(it) },
        isDeleted = isDeleted
    )
}

fun createFolder(
    name: String,
    color: Int
): FolderEntity {
    val now = System.currentTimeMillis()
    return FolderEntity(
        id = java.util.UUID.randomUUID().toString(),
        name = name,
        color = color,
        noteCount = 0,
        createdAt = now,
        modifiedAt = now,
        deletedAt = null,
        isDeleted = false
    )
}

private fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )
}

private fun localDateTimeToTimestamp(dateTime: LocalDateTime): Long {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
} 