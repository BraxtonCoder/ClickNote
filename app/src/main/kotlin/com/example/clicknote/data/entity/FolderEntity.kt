package com.example.clicknote.data.entity

import androidx.room.*
import java.time.LocalDateTime

@Entity(
    tableName = "folders",
    indices = [
        Index("name", unique = true),
        Index("created_at")
    ]
)
data class FolderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "note_count")
    val noteCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: LocalDateTime? = null
) {
    companion object {
        fun create(
            name: String,
            color: Int
        ): FolderEntity {
            val now = LocalDateTime.now()
            return FolderEntity(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                color = color,
                noteCount = 0,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
                deletedAt = null
            )
        }
    }
} 