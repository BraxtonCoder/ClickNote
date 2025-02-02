package com.example.clicknote.data.entity

import androidx.room.*

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
    indices = [Index("note_id")]
)
data class TranscriptionMetadata(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "language")
    val language: String,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "word_count")
    val wordCount: Int,

    @ColumnInfo(name = "speaker_count")
    val speakerCount: Int = 1,

    @ColumnInfo(name = "is_processed")
    val isProcessed: Boolean = false,

    @ColumnInfo(name = "processing_timestamp")
    val processingTimestamp: Long? = null,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
) 