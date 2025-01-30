package com.example.clicknote.data.entity

import androidx.room.*
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
data class TranscriptionMetadata(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "language")
    val language: String,

    @ColumnInfo(name = "speaker_count")
    val speakerCount: Int,

    @ColumnInfo(name = "duration")
    val duration: Long, // Duration in milliseconds

    @ColumnInfo(name = "word_count")
    val wordCount: Int,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,

    @ColumnInfo(name = "processing_time")
    val processingTime: Long, // Time taken to transcribe in milliseconds

    @ColumnInfo(name = "model")
    val model: String, // "whisper-tiny-en", "whisper-base", etc.

    @ColumnInfo(name = "is_offline")
    val isOffline: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) 