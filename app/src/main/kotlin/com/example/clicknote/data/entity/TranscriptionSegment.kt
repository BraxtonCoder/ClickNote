package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.clicknote.domain.model.TranscriptionSegment as DomainSegment
import java.util.UUID

@Entity(
    tableName = "transcription_segments",
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
data class TranscriptionSegment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "speaker_id")
    val speakerId: String? = null,

    @ColumnInfo(name = "confidence")
    val confidence: Float = 0f,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = DomainSegment(
        text = content,
        startTime = startTime,
        endTime = endTime,
        speaker = speakerId,
        confidence = confidence
    )

    companion object {
        fun fromDomain(noteId: String, segment: DomainSegment) = TranscriptionSegment(
            noteId = noteId,
            content = segment.text,
            startTime = segment.startTime,
            endTime = segment.endTime,
            speakerId = segment.speaker,
            confidence = segment.confidence
        )
    }
} 