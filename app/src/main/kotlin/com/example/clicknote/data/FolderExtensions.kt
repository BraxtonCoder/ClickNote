package com.example.clicknote.data

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import java.time.LocalDateTime

/**
 * Extension function to convert FolderEntity to domain Folder
 */
fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
}

/**
 * Extension function to convert domain Folder to FolderEntity
 */
fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
}

/**
 * Helper function to create a new folder entity
 */
fun createFolder(
    name: String,
    color: Int,
    parentId: String? = null,
    position: Int = 0
): FolderEntity {
    val now = LocalDateTime.now()
    return FolderEntity(
        id = java.util.UUID.randomUUID().toString(),
        name = name,
        color = color,
        createdAt = now,
        modifiedAt = now,
        isDeleted = false,
        parentId = parentId,
        position = position
    )
} 