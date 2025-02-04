package com.example.clicknote.domain.model

import java.time.LocalDateTime

data class Folder(
    val id: String,
    val name: String,
    val color: Int,
    val noteCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean = false,
    val deletedAt: LocalDateTime? = null
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
                isDeleted = false,
                deletedAt = null
            )
        }
    }
} 