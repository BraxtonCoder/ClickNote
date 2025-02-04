package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import com.example.clicknote.util.DateTimeUtils

fun FolderEntity.toFolder(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        isDeleted = isDeleted,
        deletedAt = deletedAt?.let { DateTimeUtils.timestampToLocalDateTime(it) }
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        modifiedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
        isDeleted = isDeleted,
        deletedAt = deletedAt?.let { DateTimeUtils.localDateTimeToTimestamp(it) }
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
        isDeleted = false,
        deletedAt = null
    )
} 