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
        updatedAt = timestampToLocalDateTime(updatedAt),
        deletedAt = deletedAt?.let { timestampToLocalDateTime(it) }
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = localDateTimeToTimestamp(createdAt),
        updatedAt = localDateTimeToTimestamp(updatedAt),
        deletedAt = deletedAt?.let { localDateTimeToTimestamp(it) }
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

fun FolderEntity.Companion.create(
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
        updatedAt = now,
        isDeleted = false,
        deletedAt = null
    )
} 