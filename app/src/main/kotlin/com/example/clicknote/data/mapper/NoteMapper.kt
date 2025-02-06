package com.example.clicknote.data.mapper

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.util.DateTimeUtils
import java.time.LocalDateTime

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
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        source = NoteSource.valueOf(source),
        syncStatus = SyncStatus.valueOf(syncStatus),
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = isDeleted,
        hasAudio = hasAudio,
        audioPath = audioPath,
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
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        modifiedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
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
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(modifiedAt),
        source = NoteSource.valueOf(source),
        syncStatus = SyncStatus.valueOf(syncStatus),
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
    val now = LocalDateTime.now()
    return Note(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        modifiedAt = now,
        source = source,
        syncStatus = SyncStatus.PENDING,
        folderId = folderId,
        isArchived = false,
        isPinned = false,
        isDeleted = false,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
} 