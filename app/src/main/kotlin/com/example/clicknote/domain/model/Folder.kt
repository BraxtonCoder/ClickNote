package com.example.clicknote.domain.model

import java.time.LocalDateTime

data class Folder(
    val id: String,
    val name: String,
    val color: Int,
    val noteCount: Int = 0,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val deletedAt: LocalDateTime? = null,
    val isDeleted: Boolean = false
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
                isDeleted = false
            )
        }
    }
} 