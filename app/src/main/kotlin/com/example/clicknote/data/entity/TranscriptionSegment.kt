package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.clicknote.domain.model.TranscriptionSegment as DomainSegment
import java.util.UUID

@Entity(
    tableName = "transcription_segments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("noteId"),
        Index("startTime"),
        Index("endTime")
    ]
)
data class TranscriptionSegmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val text: String,
    val startTime: Long, // Stored in milliseconds
    val endTime: Long, // Stored in milliseconds
    val confidence: Float,
    val speakerId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = DomainSegment(
        text = text,
        startTime = startTime / 1000.0, // Convert to seconds
        endTime = endTime / 1000.0, // Convert to seconds
        confidence = confidence,
        speakerId = speakerId
    )

    companion object {
        fun fromDomain(noteId: String, domain: DomainSegment) = TranscriptionSegmentEntity(
            noteId = noteId,
            text = domain.text,
            startTime = (domain.startTime * 1000).toLong(), // Convert to milliseconds
            endTime = (domain.endTime * 1000).toLong(), // Convert to milliseconds
            confidence = domain.confidence,
            speakerId = domain.speakerId
        )

        fun empty(noteId: String) = TranscriptionSegmentEntity(
            noteId = noteId,
            text = "",
            startTime = 0L,
            endTime = 0L,
            confidence = 0f
        )
    }
} 