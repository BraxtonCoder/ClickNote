package com.example.clicknote.domain.model

/**
 * Represents the source/origin of a note
 */
enum class NoteSource {
    MANUAL,         // Manually typed note
    VOICE,          // Voice recording transcription
    IMPORT,         // Imported from file
    CALL,           // Phone call transcription
    SHARE,          // Shared from another app
    QUICK_NOTE,     // Quick note (< 30 seconds)
    LONG_NOTE;      // Long note (>= 30 seconds)

    companion object {
        fun fromString(source: String): NoteSource {
            return try {
                valueOf(source.uppercase())
            } catch (e: IllegalArgumentException) {
                MANUAL
            }
        }

        fun fromRecordingDuration(durationSeconds: Int): NoteSource {
            return if (durationSeconds < 30) QUICK_NOTE else LONG_NOTE
        }
    }
} 