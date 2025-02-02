package com.example.clicknote.domain.model

import com.example.clicknote.data.entity.FolderEntity

data class Folder(
    val id: String,
    val name: String,
    val color: Int,
    val sortOrder: Int = 0,
    val syncStatus: Int = 0,
    val createdAt: Long,
    val modifiedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    fun toEntity() = FolderEntity(
        id = id,
        name = name,
        color = color,
        sortOrder = sortOrder,
        syncStatus = syncStatus,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )

    companion object {
        fun create(
            name: String,
            color: Int,
            sortOrder: Int = 0,
            syncStatus: Int = 0
        ): Folder {
            val now = System.currentTimeMillis()
            return Folder(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                color = color,
                sortOrder = sortOrder,
                syncStatus = syncStatus,
                createdAt = now,
                modifiedAt = now
            )
        }
    }
} 