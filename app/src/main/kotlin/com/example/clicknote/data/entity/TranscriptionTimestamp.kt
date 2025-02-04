package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.data.entity.NoteEntity
import java.util.UUID

@Entity(
    tableName = "transcription_timestamps",
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
data class TranscriptionTimestamp(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "note_id")
    val noteId: String,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "start_time")
    val startTime: Long, // milliseconds

    @ColumnInfo(name = "end_time")
    val endTime: Long, // milliseconds

    @ColumnInfo(name = "speaker")
    val speaker: String? = null,

    @ColumnInfo(name = "confidence")
    val confidence: Float = 0f
) {
    /**
     * Converts the entity to a domain model TranscriptionSegment.
     * Converts timestamps from milliseconds (Long) to seconds (Double).
     */
    fun toSegment() = TranscriptionSegment(
        id = id,
        noteId = noteId,
        text = text,
        startTime = startTime,
        endTime = endTime,
        speaker = speaker,
        confidence = confidence
    )

    companion object {
        /**
         * Creates a TranscriptionTimestamp entity from a domain model TranscriptionSegment.
         * Converts timestamps from seconds (Double) to milliseconds (Long).
         */
        fun fromSegment(noteId: String, segment: TranscriptionSegment): TranscriptionTimestamp {
            return TranscriptionTimestamp(
                id = segment.id,
                noteId = noteId,
                text = segment.text,
                startTime = segment.startTime,
                endTime = segment.endTime,
                speaker = segment.speaker,
                confidence = segment.confidence
            )
        }
    }
} 