package com.example.clicknote.data.mapper

import com.example.clicknote.domain.model.Folder
import com.example.clicknote.util.DateTimeUtils
import java.time.LocalDateTime

data class FolderDto(
    val id: String = "",
    val name: String = "",
    val color: Int = 0,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val isDeleted: Boolean = false,
    val parentId: String? = null,
    val position: Int = 0
)

fun Folder.toDto(): FolderDto {
    return FolderDto(
        id = id,
        name = name,
        color = color,
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        modifiedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
}

fun FolderDto.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
}

fun createFolder(
    name: String,
    color: Int,
    parentId: String? = null,
    position: Int = 0
): Folder {
    val now = LocalDateTime.now()
    return Folder(
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