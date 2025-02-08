package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.domain.model.Folder

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("name", unique = true),
        Index("created_at"),
        Index("parent_id"),
        Index("position")
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

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "parent_id")
    val parentId: String? = null,

    @ColumnInfo(name = "position")
    val position: Int = 0
) {
    fun toDomain(): Folder {
        return Folder(
            id = id,
            name = name,
            color = color,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            isDeleted = isDeleted,
            deletedAt = deletedAt,
            parentId = parentId,
            position = position
        )
    }

    companion object {
        fun fromDomain(domain: Folder): FolderEntity {
            return FolderEntity(
                id = domain.id,
                name = domain.name,
                color = domain.color,
                createdAt = domain.createdAt,
                modifiedAt = domain.modifiedAt,
                isDeleted = domain.isDeleted,
                deletedAt = domain.deletedAt,
                parentId = domain.parentId,
                position = domain.position
            )
        }

        fun create(
            name: String,
            color: Int,
            parentId: String? = null,
            position: Int = 0
        ): FolderEntity {
            val now = System.currentTimeMillis()
            return FolderEntity(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                color = color,
                createdAt = now,
                modifiedAt = now,
                isDeleted = false,
                deletedAt = null,
                parentId = parentId,
                position = position
            )
        }
    }

    val isRoot: Boolean
        get() = parentId == null

    val hasParent: Boolean
        get() = parentId != null
}

/**
 * Extension function to convert FolderEntity to domain Folder
 */
fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        parentId = parentId,
        position = position
    )
}

/**
 * Extension function to convert domain Folder to FolderEntity
 */
fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        parentId = parentId,
        position = position
    )
} 