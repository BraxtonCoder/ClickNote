package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.model.TranscriptionState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

data class NoteDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val source: String = NoteSource.MANUAL.name,
    val syncStatus: String = SyncStatus.PENDING.name,
    val folderId: String? = null,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val hasAudio: Boolean = false,
    val audioPath: String? = null,
    val duration: Int? = null,
    val transcriptionLanguage: String? = null,
    val speakerCount: Int? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Converts a NoteEntity to a domain Note model
 */
fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        summary = transcription,
        audioPath = audioPath,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = deletedAt,
        folderId = folderId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.fromString(syncStatus),
        source = NoteSource.fromString(source),
        isArchived = isArchived,
        duration = duration?.toInt(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Converts a NoteWithFolderEntity to a domain Note model
 */
fun NoteWithFolderEntity.toNote(): Note = note.toDomain().copy(
    folderId = folder?.id
)

/**
 * Converts a domain Note model to a NoteEntity
 */
fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        transcription = content,
        summary = "",
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = null,
        source = source.name,
        syncStatus = syncStatus.name,
        transcriptionState = TranscriptionState.COMPLETED.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = isDeleted,
        audioPath = audioPath,
        duration = duration?.toLong(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Converts a domain Note model to a NoteDto
 */
fun Note.toDto(): NoteDto {
    return NoteDto(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        source = source.name,
        syncStatus = syncStatus.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = isDeleted,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Converts a NoteDto to a domain Note model
 */
fun NoteDto.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        summary = "",
        audioPath = audioPath,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = null,
        folderId = folderId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.fromString(syncStatus),
        source = NoteSource.fromString(source),
        isArchived = isArchived,
        duration = duration,
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Creates a new Note with default values
 */
fun createNote(
    title: String,
    content: String,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    audioPath: String? = null,
    duration: Int? = null,
    transcriptionLanguage: String? = null,
    speakerCount: Int? = null,
    metadata: Map<String, String> = emptyMap()
): Note {
    val now = Date().time
    return Note(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        summary = "",
        audioPath = audioPath,
        createdAt = now,
        modifiedAt = now,
        deletedAt = null,
        folderId = folderId,
        isPinned = false,
        isDeleted = false,
        syncStatus = SyncStatus.PENDING,
        source = source,
        isArchived = false,
        duration = duration,
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

object NoteMapper {
    fun fromDocument(document: DocumentSnapshot): NoteEntity? {
        return try {
            val content = document.getString("content") ?: ""
            val transcription = document.getString("transcription") ?: content
            
            NoteEntity(
                id = document.id,
                title = document.getString("title") ?: "",
                content = content,
                transcription = transcription,
                summary = document.getString("summary"),
                createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                modifiedAt = document.getTimestamp("modifiedAt")?.toDate()?.time ?: System.currentTimeMillis(),
                deletedAt = document.getTimestamp("deletedAt")?.toDate()?.time,
                source = document.getString("source") ?: NoteSource.MANUAL.name,
                syncStatus = document.getString("syncStatus") ?: SyncStatus.PENDING.name,
                transcriptionState = document.getString("transcriptionState") ?: TranscriptionState.COMPLETED.name,
                folderId = document.getString("folderId"),
                isArchived = document.getBoolean("isArchived") ?: false,
                isPinned = document.getBoolean("isPinned") ?: false,
                isDeleted = document.getBoolean("isDeleted") ?: false,
                audioPath = document.getString("audioPath"),
                duration = document.getLong("duration"),
                transcriptionLanguage = document.getString("transcriptionLanguage"),
                speakerCount = document.getLong("speakerCount")?.toInt(),
                metadata = (document.get("metadata") as? Map<String, String>) ?: emptyMap()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun toDocument(entity: NoteEntity): Map<String, Any?> {
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
} 