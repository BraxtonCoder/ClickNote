package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.model.TranscriptionState
import java.util.UUID

/**
 * Room entity representing a note in the database
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("folder_id"),
        Index("created_at"),
        Index("modified_at"),
        Index("is_deleted"),
        Index("sync_status"),
        Index("duration"),
        Index("is_archived"),
        Index("is_pinned"),
        Index("source"),
        Index("title"),
        Index("transcription_state")
    ]
)
@TypeConverters(RoomConverters::class)
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content") 
    val content: String,
    
    @ColumnInfo(name = "transcription")
    val transcription: String,
    
    @ColumnInfo(name = "summary")
    val summary: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
    
    @ColumnInfo(name = "source")
    val source: String = NoteSource.MANUAL.name,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,
    
    @ColumnInfo(name = "transcription_state")
    val transcriptionState: String = TranscriptionState.PENDING.name,
    
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean = false,
    
    @ColumnInfo(name = "audio_path")
    val audioPath: String? = null,
    
    @ColumnInfo(name = "duration")
    val duration: Long? = null,
    
    @ColumnInfo(name = "transcription_language")
    val transcriptionLanguage: String? = DEFAULT_LANGUAGE,
    
    @ColumnInfo(name = "speaker_count")
    val speakerCount: Int? = null,
    
    @ColumnInfo(name = "metadata", typeAffinity = ColumnInfo.BLOB)
    val metadata: Map<String, String> = emptyMap()
) {
    val isLongNote: Boolean
        get() = duration?.let { it >= LONG_NOTE_THRESHOLD_MS } ?: false

    val isQuickNote: Boolean
        get() = !isLongNote

    val isTranscriptionComplete: Boolean
        get() = transcriptionState == TranscriptionState.COMPLETED.name

    val isTranscriptionFailed: Boolean
        get() = transcriptionState == TranscriptionState.FAILED.name

    val isExternalSource: Boolean
        get() = NoteSource.isExternalSource(NoteSource.fromString(source))

    val requiresNotification: Boolean
        get() = NoteSource.requiresNotification(NoteSource.fromString(source))

    val requiresPermission: Boolean
        get() = NoteSource.requiresPermission(NoteSource.fromString(source))

    fun validate(): Boolean {
        return title.isNotBlank() &&
               content.isNotBlank() &&
               transcription.isNotBlank() &&
               createdAt > 0 &&
               modifiedAt >= createdAt &&
               (deletedAt == null || deletedAt >= modifiedAt) &&
               (duration == null || duration > 0) &&
               (speakerCount == null || speakerCount > 0)
    }

    companion object {
        const val LONG_NOTE_THRESHOLD_MS = 30_000L // 30 seconds in milliseconds
        const val MAX_TITLE_LENGTH = 100
        const val DEFAULT_LANGUAGE = "en"

        fun create(
            title: String,
            content: String,
            audioPath: String? = null,
            duration: Long? = null,
            source: String = NoteSource.MANUAL.name,
            folderId: String? = null,
            transcriptionLanguage: String? = DEFAULT_LANGUAGE,
            summary: String? = null
        ): NoteEntity {
            val now = System.currentTimeMillis()
            return NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title.take(MAX_TITLE_LENGTH).trim(),
                content = content.trim(),
                transcription = content.trim(),
                summary = summary?.trim(),
                createdAt = now,
                modifiedAt = now,
                source = source,
                syncStatus = SyncStatus.PENDING.name,
                transcriptionState = TranscriptionState.PENDING.name,
                folderId = folderId,
                isArchived = false,
                isPinned = false,
                isDeleted = false,
                hasAudio = audioPath != null,
                audioPath = audioPath,
                duration = duration,
                transcriptionLanguage = transcriptionLanguage ?: DEFAULT_LANGUAGE,
                speakerCount = null,
                metadata = emptyMap()
            )
        }
    }
} 