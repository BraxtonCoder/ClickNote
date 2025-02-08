package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.FolderEntity
import com.example.clicknote.domain.model.Folder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

object FolderMapper {
    fun toEntity(domain: Folder): FolderEntity {
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

    fun toDomain(entity: FolderEntity): Folder {
        return Folder(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt,
            parentId = entity.parentId,
            position = entity.position
        )
    }

    fun fromDocument(document: DocumentSnapshot): FolderEntity? {
        return try {
            FolderEntity(
                id = document.id,
                name = document.getString("name") ?: "",
                color = document.getLong("color")?.toInt() ?: 0,
                createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: System.currentTimeMillis(),
                isDeleted = document.getBoolean("isDeleted") ?: false,
                deletedAt = document.getTimestamp("deletedAt")?.toDate()?.time,
                parentId = document.getString("parentId"),
                position = document.getLong("position")?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    fun toDocument(entity: FolderEntity): Map<String, Any?> {
        return mapOf(
            "name" to entity.name,
            "color" to entity.color,
            "createdAt" to Timestamp(Date(entity.createdAt)),
            "modifiedAt" to Timestamp(Date(entity.modifiedAt)),
            "deletedAt" to entity.deletedAt?.let { Timestamp(Date(it)) },
            "isDeleted" to entity.isDeleted,
            "parentId" to entity.parentId,
            "position" to entity.position
        )
    }
} 