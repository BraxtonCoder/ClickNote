package com.example.clicknote.data

import com.example.clicknote.data.entity.NoteEntity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.util.DateTimeUtils
import java.time.LocalDateTime
import java.util.UUID
import com.example.clicknote.data.entity.NoteWithFolderEntity

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        createdAt = DateTimeUtils.timestampToLocalDateTime(createdAt),
        modifiedAt = DateTimeUtils.timestampToLocalDateTime(updatedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.timestampToLocalDateTime(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        source = NoteSource.valueOf(source),
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        createdAt = DateTimeUtils.localDateTimeToTimestamp(createdAt),
        updatedAt = DateTimeUtils.localDateTimeToTimestamp(modifiedAt),
        deletedAt = deletedAt?.let { DateTimeUtils.localDateTimeToTimestamp(it) },
        isDeleted = isDeleted,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = hasAudio,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = syncStatus.name
    )
}

fun Note.copy(
    id: String = this.id,
    title: String = this.title,
    content: String = this.content,
    createdAt: LocalDateTime = this.createdAt,
    modifiedAt: LocalDateTime = this.modifiedAt,
    deletedAt: LocalDateTime? = this.deletedAt,
    isDeleted: Boolean = this.isDeleted,
    isPinned: Boolean = this.isPinned,
    isLongForm: Boolean = this.isLongForm,
    hasAudio: Boolean = this.hasAudio,
    audioPath: String? = this.audioPath,
    duration: Long = this.duration,
    source: NoteSource = this.source,
    folderId: String? = this.folderId,
    summary: String? = this.summary,
    keyPoints: List<String> = this.keyPoints,
    speakers: List<String> = this.speakers,
    syncStatus: SyncStatus = this.syncStatus
): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    deletedAt = deletedAt,
    isDeleted = isDeleted,
    isPinned = isPinned,
    isLongForm = isLongForm,
    hasAudio = hasAudio,
    audioPath = audioPath,
    duration = duration,
    source = source,
    folderId = folderId,
    summary = summary,
    keyPoints = keyPoints,
    speakers = speakers,
    syncStatus = syncStatus
)

fun createNote(
    title: String,
    content: String,
    isLongForm: Boolean = false,
    audioPath: String? = null,
    duration: Long = 0L,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null
): NoteEntity {
    val now = System.currentTimeMillis()
    return NoteEntity(
        id = UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        isDeleted = false,
        isPinned = false,
        isLongForm = isLongForm,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = null,
        keyPoints = emptyList(),
        speakers = emptyList(),
        syncStatus = SyncStatus.PENDING.name
    )
}

fun NoteWithFolderEntity.toNote(): Note = note.toDomain().copy(
    folderId = folder?.id
)

fun NoteEntity.Companion.create(
    title: String,
    content: String,
    summary: String? = null,
    keyPoints: List<String> = emptyList(),
    speakers: List<String> = emptyList(),
    audioPath: String? = null,
    duration: Long = 0L,
    source: NoteSource = NoteSource.MANUAL,
    folderId: String? = null,
    isPinned: Boolean = false,
    isLongForm: Boolean = false
): NoteEntity {
    val now = System.currentTimeMillis()
    return NoteEntity(
        id = UUID.randomUUID().toString(),
        title = title,
        content = content,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        isDeleted = false,
        isPinned = isPinned,
        isLongForm = isLongForm,
        hasAudio = audioPath != null,
        audioPath = audioPath,
        duration = duration,
        source = source.name,
        folderId = folderId,
        summary = summary,
        keyPoints = keyPoints,
        speakers = speakers,
        syncStatus = SyncStatus.PENDING.name
    )
} 