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
    val deletedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false,
    val sortOrder: Int = 0,
    val noteCount: Int = 0
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
                noteCount = 0,
                createdAt = now,
                modifiedAt = now,
                deletedAt = null,
                isDeleted = false,
                sortOrder = 0
            )
        }
    }
} 