package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import java.time.LocalDateTime

fun FolderEntity.toFolder(): Folder = Folder(
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

fun Folder.toFolderEntity(): FolderEntity = FolderEntity(
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