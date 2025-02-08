package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.*
import com.example.clicknote.domain.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

data class FirebaseNote(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val duration: Int? = null,
    val source: String = NoteSource.MANUAL.name,
    val folderId: String? = null,
    val platform: String = "android",
    val syncStatus: String = SyncStatus.PENDING.name
)

data class FirebaseFolder(
    val id: String = "",
    val name: String = "",
    val color: Int = 0,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val isDeleted: Boolean = false,
    val parentId: String? = null,
    val position: Int = 0
)

fun Note.toFirebaseNote(): FirebaseNote {
    return FirebaseNote(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        isPinned = isPinned,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        platform = "android",
        syncStatus = syncStatus.name
    )
}

fun FirebaseNote.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        isPinned = isPinned,
        audioPath = audioPath,
        duration = duration,
        source = NoteSource.valueOf(source),
        folderId = folderId,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
}

fun Folder.toFirebaseFolder(): FirebaseFolder {
    return FirebaseFolder(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
}

fun FirebaseFolder.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isDeleted = isDeleted,
        parentId = parentId,
        position = position
    )
}

object FirebaseMapper {
    fun mapToNoteEntity(document: DocumentSnapshot): NoteEntity? {
        return try {
            NoteEntity.create(
                title = document.getString("title") ?: "",
                content = document.getString("content") ?: "",
                audioPath = document.getString("audioPath"),
                duration = document.getLong("duration"),
                source = document.getString("source") ?: NoteSource.MANUAL.name,
                folderId = document.getString("folderId"),
                transcriptionLanguage = document.getString("transcriptionLanguage"),
                summary = document.getString("summary")
            ).copy(
                id = document.id,
                createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: System.currentTimeMillis(),
                deletedAt = document.getTimestamp("deletedAt")?.toDate()?.time,
                isArchived = document.getBoolean("isArchived") ?: false,
                isPinned = document.getBoolean("isPinned") ?: false,
                isDeleted = document.getBoolean("isDeleted") ?: false,
                speakerCount = document.getLong("speakerCount")?.toInt(),
                metadata = (document.get("metadata") as? Map<String, String>) ?: emptyMap()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun mapToFolderEntity(document: DocumentSnapshot): FolderEntity? {
        return try {
            FolderEntity.create(
                name = document.getString("name") ?: "",
                color = document.getLong("color")?.toInt() ?: 0,
                parentId = document.getString("parentId"),
                position = document.getLong("position")?.toInt() ?: 0
            ).copy(
                id = document.id,
                createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: System.currentTimeMillis(),
                isDeleted = document.getBoolean("isDeleted") ?: false,
                deletedAt = document.getTimestamp("deletedAt")?.toDate()?.time
            )
        } catch (e: Exception) {
            null
        }
    }

    fun mapToFirebaseNote(entity: NoteEntity): Map<String, Any?> {
        return mapOf(
            "title" to entity.title,
            "content" to entity.content,
            "transcription" to entity.transcription,
            "summary" to entity.summary,
            "createdAt" to Timestamp(Date(entity.createdAt)),
            "modifiedAt" to Timestamp(Date(entity.modifiedAt)),
            "deletedAt" to entity.deletedAt?.let { Timestamp(Date(it)) },
            "source" to entity.source,
            "syncStatus" to entity.syncStatus,
            "transcriptionState" to entity.transcriptionState,
            "folderId" to entity.folderId,
            "isArchived" to entity.isArchived,
            "isPinned" to entity.isPinned,
            "isDeleted" to entity.isDeleted,
            "audioPath" to entity.audioPath,
            "duration" to entity.duration,
            "transcriptionLanguage" to entity.transcriptionLanguage,
            "speakerCount" to entity.speakerCount,
            "metadata" to entity.metadata
        )
    }

    fun mapToFirebaseFolder(entity: FolderEntity): Map<String, Any?> {
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