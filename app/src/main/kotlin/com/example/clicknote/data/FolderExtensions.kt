package com.example.clicknote.data

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import java.time.LocalDateTime

fun FolderEntity.toFolder(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
}

fun FolderEntity.Companion.create(
    name: String,
    color: Int
): FolderEntity {
    val now = LocalDateTime.now()
    return FolderEntity(
        id = java.util.UUID.randomUUID().toString(),
        name = name,
        color = color,
        createdAt = now,
        modifiedAt = now,
        isDeleted = false,
        deletedAt = null
    )
} 