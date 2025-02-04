package com.example.clicknote.data

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder

fun FolderEntity.toFolder(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        sortOrder = sortOrder,
        syncStatus = syncStatus,
        createdAt = createdAt,
        modifiedAt = updatedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        sortOrder = sortOrder,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )
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
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
        deletedAt = null
    )
} 