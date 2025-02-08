package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.domain.model.TranscriptionLanguage
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID

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
    val language: String = DEFAULT_LANGUAGE,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0L,

    @ColumnInfo(name = "word_count")
    val wordCount: Int = 0,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float = 0f,

    @ColumnInfo(name = "processing_time")
    val processingTime: Long = 0L,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "speaker_count")
    val speakerCount: Int = 1,

    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean = false,

    @ColumnInfo(name = "is_multi_speaker")
    val isMultiSpeaker: Boolean = false,

    @ColumnInfo(name = "speaker_labels")
    val speakerLabels: List<String> = emptyList(),

    @ColumnInfo(name = "timestamps")
    val timestamps: List<Long> = emptyList(),

    @ColumnInfo(name = "segments")
    val segments: List<TranscriptionSegment> = emptyList(),

    @ColumnInfo(name = "metadata")
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val MIN_SPEAKER_COUNT = 1
        const val MAX_SPEAKER_COUNT = 10
        const val MIN_CONFIDENCE = 0f
        const val MAX_CONFIDENCE = 1f

        fun create(
            noteId: String,
            language: String = DEFAULT_LANGUAGE,
            speakerCount: Int = 1,
            durationMs: Long = 0L,
            hasAudio: Boolean = false
        ): TranscriptionMetadata {
            val now = System.currentTimeMillis()
            return TranscriptionMetadata(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                language = language.takeIf { it.isNotBlank() } ?: DEFAULT_LANGUAGE,
                speakerCount = speakerCount.coerceIn(MIN_SPEAKER_COUNT, MAX_SPEAKER_COUNT),
                durationMs = durationMs.coerceAtLeast(0L),
                hasAudio = hasAudio,
                createdAt = now,
                updatedAt = now
            )
        }

        fun createEmpty(noteId: String): TranscriptionMetadata {
            val now = System.currentTimeMillis()
            return TranscriptionMetadata(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun isValid(): Boolean {
        return language.isNotBlank() &&
                speakerCount in MIN_SPEAKER_COUNT..MAX_SPEAKER_COUNT &&
                durationMs >= 0 &&
                confidenceScore in MIN_CONFIDENCE..MAX_CONFIDENCE &&
                (!isMultiSpeaker || speakerLabels.isNotEmpty()) &&
                segments.all { it.isValid() }
    }

    fun withLanguage(language: String): TranscriptionMetadata {
        return copy(
            language = language.takeIf { it.isNotBlank() } ?: DEFAULT_LANGUAGE,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun withSpeakerCount(count: Int): TranscriptionMetadata {
        val validCount = count.coerceIn(MIN_SPEAKER_COUNT, MAX_SPEAKER_COUNT)
        return copy(
            speakerCount = validCount,
            isMultiSpeaker = validCount > 1,
            speakerLabels = if (validCount > 1) {
                (1..validCount).map { "Speaker $it" }
            } else {
                emptyList()
            },
            updatedAt = System.currentTimeMillis()
        )
    }

    fun withDuration(durationMs: Long): TranscriptionMetadata {
        return copy(
            durationMs = durationMs.coerceAtLeast(0L),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun withConfidence(confidence: Float): TranscriptionMetadata {
        return copy(
            confidenceScore = confidence.coerceIn(MIN_CONFIDENCE, MAX_CONFIDENCE),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun addSegment(segment: TranscriptionSegment): TranscriptionMetadata {
        return if (segment.isValid()) {
            copy(
                segments = segments + segment,
                wordCount = wordCount + segment.text.split("\\s+".toRegex()).count(),
                updatedAt = System.currentTimeMillis()
            )
        } else this
    }

    fun addMetadata(key: String, value: String): TranscriptionMetadata {
        return copy(
            metadata = metadata + (key to value),
            updatedAt = System.currentTimeMillis()
        )
    }
}

@Serializable
data class TranscriptionSegment(
    val startTime: Long,
    val endTime: Long,
    val text: String,
    val speakerLabel: String? = null,
    val confidence: Float = 0f
) {
    fun isValid(): Boolean {
        return startTime >= 0 &&
                endTime > startTime &&
                text.isNotBlank() &&
                confidence in TranscriptionMetadata.MIN_CONFIDENCE..TranscriptionMetadata.MAX_CONFIDENCE
    }

    companion object {
        fun create(
            startTime: Long,
            endTime: Long,
            text: String,
            speakerLabel: String? = null,
            confidence: Float = 0f
        ): TranscriptionSegment {
            return TranscriptionSegment(
                startTime = startTime.coerceAtLeast(0L),
                endTime = endTime.coerceAtLeast(startTime + 1),
                text = text.trim(),
                speakerLabel = speakerLabel?.takeIf { it.isNotBlank() },
                confidence = confidence.coerceIn(
                    TranscriptionMetadata.MIN_CONFIDENCE,
                    TranscriptionMetadata.MAX_CONFIDENCE
                )
            )
        }
    }
} 