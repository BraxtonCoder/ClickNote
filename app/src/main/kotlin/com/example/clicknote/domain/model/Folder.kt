package com.example.clicknote.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a folder that can contain notes
 */
data class Folder(
    val id: String,
    val name: String,
    val color: Int,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val isDeleted: Boolean = false,
    val parentId: String? = null,
    val position: Int = 0
) {
    companion object {
        fun create(
            name: String,
            color: Int
        ): Folder {
            val now = LocalDateTime.now()
            return Folder(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                color = color,
                createdAt = now,
                modifiedAt = now,
                isDeleted = false,
                parentId = null,
                position = 0
            )
        }

        fun createEmpty() = Folder(
            id = "",
            name = "",
            color = 0,
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now(),
            isDeleted = false,
            parentId = null,
            position = 0
        )
    }
} 