package com.example.clicknote.data.entity

import androidx.room.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.example.clicknote.domain.model.Folder

@Entity(
    tableName = "folders",
    indices = [
        Index("name", unique = true),
        Index("created_at"),
        Index("sort_order")
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
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
) {
    fun toDomain(): Folder {
        return Folder(
            id = id,
            name = name,
            color = color,
            createdAt = LocalDateTime.ofEpochSecond(createdAt / 1000, 0, ZoneOffset.UTC),
            modifiedAt = LocalDateTime.ofEpochSecond(modifiedAt / 1000, 0, ZoneOffset.UTC),
            deletedAt = deletedAt?.let { LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC) },
            isDeleted = isDeleted,
            sortOrder = sortOrder,
            noteCount = noteCount
        )
    }

    companion object {
        fun fromDomain(domain: Folder): FolderEntity {
            return FolderEntity(
                id = domain.id,
                name = domain.name,
                color = domain.color,
                noteCount = domain.noteCount,
                createdAt = domain.createdAt.toEpochSecond(ZoneOffset.UTC) * 1000,
                modifiedAt = domain.modifiedAt.toEpochSecond(ZoneOffset.UTC) * 1000,
                deletedAt = domain.deletedAt?.toEpochSecond(ZoneOffset.UTC)?.times(1000),
                isDeleted = domain.isDeleted,
                sortOrder = domain.sortOrder
            )
        }

        fun create(
            name: String,
            color: Int
        ): FolderEntity {
            val now = System.currentTimeMillis()
            return FolderEntity(
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