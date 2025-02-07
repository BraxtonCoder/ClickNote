package com.example.clicknote.domain.model

import java.util.UUID

/**
 * Represents a segment of transcribed audio with timing information.
 * @property id The unique identifier for this segment
 * @property noteId The identifier for the note this segment belongs to
 * @property text The transcribed text for this segment
 * @property startTime The start time of the segment in milliseconds
 * @property endTime The end time of the segment in milliseconds
 * @property speaker Optional identifier for the speaker in this segment
 * @property confidence The confidence score for this segment
 * @property createdAt The timestamp when this segment was created
 * @property updatedAt The timestamp when this segment was last updated
 */
data class TranscriptionSegment(
    val text: String,
    val startTime: Double,
    val endTime: Double,
    val confidence: Float,
    val speakerId: String? = null
) {
    companion object {
        /**
         * Creates an empty segment with default values
         */
        fun empty(noteId: String) = TranscriptionSegment(
            text = "",
            startTime = 0.0,
            endTime = 0.0,
            confidence = 0f
        )
    }
} 