package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import java.time.LocalDateTime

fun FolderEntity.toFolder(): Folder = Folder(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt,
    modifiedAt = modifiedAt
)

fun Folder.toFolderEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt,
    modifiedAt = modifiedAt
) 