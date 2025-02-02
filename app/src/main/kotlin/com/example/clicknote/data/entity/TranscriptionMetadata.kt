package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import java.time.LocalDateTime

@Entity(
    tableName = "transcription_metadata",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("note_id")
    ]
)
@TypeConverters(RoomConverters::class)
data class TranscriptionMetadata(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "language")
    val language: String,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,

    @ColumnInfo(name = "word_count")
    val wordCount: Int,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,

    @ColumnInfo(name = "processing_time")
    val processingTime: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "speaker_count")
    val speakerCount: Int = 1,

    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean = false
) 