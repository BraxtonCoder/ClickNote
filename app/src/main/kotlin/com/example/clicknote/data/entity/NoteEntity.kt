package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import java.time.LocalDateTime

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
        Index("sync_status")
    ]
)
@TypeConverters(RoomConverters::class)
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: LocalDateTime,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_long_form")
    val isLongForm: Boolean = false,

    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean = false,

    @ColumnInfo(name = "audio_path")
    val audioPath: String? = null,

    @ColumnInfo(name = "duration")
    val duration: Long = 0,

    @ColumnInfo(name = "source")
    val source: String = NoteSource.MANUAL.name,

    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "key_points")
    val keyPoints: List<String> = emptyList(),

    @ColumnInfo(name = "speakers")
    val speakers: List<String> = emptyList(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name
) {
    fun toDomain(): Note {
        return Note(
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
            source = NoteSource.valueOf(source),
            folderId = folderId,
            summary = summary,
            keyPoints = keyPoints,
            speakers = speakers,
            syncStatus = SyncStatus.valueOf(syncStatus)
        )
    }

    companion object {
        fun fromDomain(domain: Note): NoteEntity {
            return NoteEntity(
                id = domain.id,
                title = domain.title,
                content = domain.content,
                createdAt = domain.createdAt,
                modifiedAt = domain.modifiedAt,
                deletedAt = domain.deletedAt,
                isDeleted = domain.isDeleted,
                isPinned = domain.isPinned,
                isLongForm = domain.isLongForm,
                hasAudio = domain.hasAudio,
                audioPath = domain.audioPath,
                duration = domain.duration,
                source = domain.source.name,
                folderId = domain.folderId,
                summary = domain.summary,
                keyPoints = domain.keyPoints,
                speakers = domain.speakers,
                syncStatus = domain.syncStatus.name
            )
        }
    }
} 