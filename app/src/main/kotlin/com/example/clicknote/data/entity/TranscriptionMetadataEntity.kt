package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transcription_metadata",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("noteId")
    ]
)
data class TranscriptionMetadataEntity(
    @PrimaryKey
    val id: String,
    val noteId: String,
    val language: String,
    val model: String,
    val duration: Long,
    val wordCount: Int,
    val speakerCount: Int,
    val confidence: Float,
    val processingTime: Long,
    val timestamp: Long = System.currentTimeMillis()
) 