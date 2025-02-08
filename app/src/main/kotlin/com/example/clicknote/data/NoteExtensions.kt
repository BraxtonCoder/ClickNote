package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.data.entity.NoteWithFolderEntity
import com.example.clicknote.data.model.Note as DataNote
import com.example.clicknote.domain.model.Note as DomainNote
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Extension function to convert a NoteEntity to a domain Note
 */
fun NoteEntity.toDomain(): DomainNote {
    return DomainNote(
        id = id,
        title = title,
        content = content,
        summary = summary,
        audioPath = audioPath,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        folderId = folderId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
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
 * Extension function to convert a domain Note to a NoteEntity
 */
fun DomainNote.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        transcription = content,
        summary = summary,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = deletedAt,
        source = source.name,
        syncStatus = syncStatus.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = isDeleted,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration?.toLong(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

/**
 * Extension function to convert a domain Note to a DataNote
 */
fun DomainNote.toDataModel(): DataNote {
    return DataNote(
        id = id,
        title = title,
        content = content,
        transcription = content,
        summary = summary,
        audioPath = audioPath,
        folderId = folderId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        deletedAt = deletedAt,
        syncStatus = syncStatus
    )
}

/**
 * Helper function to create a new note entity
 */
fun createNote(
    title: String,
    content: String,
    audioPath: String? = null,
    duration: Int? = null,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    isPinned: Boolean = false,
    isArchived: Boolean = false,
    transcriptionLanguage: String? = null
): NoteEntity {
    val now = System.currentTimeMillis()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        transcription = content,
        createdAt = now,
        modifiedAt = now,
        source = source.name,
        syncStatus = SyncStatus.PENDING.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = false,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration?.toLong(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = null,
        metadata = emptyMap()
    )
}

fun NoteWithFolderEntity.toNote(): DomainNote = note.toDomain().copy(
    folderId = folder?.id
)

/**
 * Helper function to create a new note entity with additional options
 */
fun createNoteWithOptions(
    title: String,
    content: String,
    audioPath: String? = null,
    duration: Int? = null,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    isPinned: Boolean = false,
    isArchived: Boolean = false,
    transcriptionLanguage: String? = null,
    speakerCount: Int? = null,
    metadata: Map<String, String> = emptyMap(),
    summary: String? = null
): NoteEntity {
    val now = System.currentTimeMillis()
    return NoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        title = title,
        content = content,
        transcription = content,
        summary = summary,
        createdAt = now,
        modifiedAt = now,
        deletedAt = null,
        source = source.name,
        syncStatus = SyncStatus.PENDING.name,
        folderId = folderId,
        isArchived = isArchived,
        isPinned = isPinned,
        isDeleted = false,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration?.toLong(),
        transcriptionLanguage = transcriptionLanguage,
        speakerCount = speakerCount,
        metadata = metadata
    )
}

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        ZoneId.systemDefault()
    )
}

fun LocalDateTime.toEpochMilli(): Long {
    return this.atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
} 