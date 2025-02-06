package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import java.time.LocalDateTime

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
        Index("sync_status")
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
    val createdAt: LocalDateTime,
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: LocalDateTime,
    
    @ColumnInfo(name = "source")
    val source: String,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String,
    
    @ColumnInfo(name = "folder_id")
    val folderId: String?,
    
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
    
    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean,
    
    @ColumnInfo(name = "audio_path")
    val audioPath: String?,
    
    @ColumnInfo(name = "duration")
    val duration: Long?,
    
    @ColumnInfo(name = "transcription_language")
    val transcriptionLanguage: String?,
    
    @ColumnInfo(name = "speaker_count")
    val speakerCount: Int?,
    
    @ColumnInfo(name = "metadata")
    val metadata: Map<String, String>
) 