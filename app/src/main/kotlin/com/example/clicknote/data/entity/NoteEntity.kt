package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.data.model.SyncStatus
import java.time.LocalDateTime

@Entity(
    tableName = "note_entity",
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
        Index("updated_at"),
        Index("is_deleted")
    ]
)
@TypeConverters(RoomConverters::class)
data class NoteEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean = false,

    @ColumnInfo(name = "audio_path")
    val audioPath: String? = null,

    @ColumnInfo(name = "source")
    val source: String = "MANUAL",

    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "key_points")
    val keyPoints: List<String> = emptyList(),

    @ColumnInfo(name = "speakers")
    val speakers: List<String> = emptyList(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
) 