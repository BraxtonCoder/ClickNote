package com.example.clicknote.domain.model

/**
 * Domain model representing a folder that can contain notes
 */
data class Folder(
    val id: String,
    val name: String,
    val color: Int,
    val createdAt: Long,
    val modifiedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val parentId: String? = null,
    val position: Int = 0
) {
    companion object {
        fun create(
            name: String,
            color: Int,
            parentId: String? = null,
            position: Int = 0
        ): Folder {
            val now = System.currentTimeMillis()
            return Folder(
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

        fun createEmpty() = Folder(
            id = "",
            name = "",
            color = 0,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            isDeleted = false,
            deletedAt = null,
            parentId = null,
            position = 0
        )
    }

    val isRoot: Boolean
        get() = parentId == null

    val hasParent: Boolean
        get() = parentId != null
} 