package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.service.AnalyticsService
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Singleton
class RecordingAnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsService: AnalyticsService
) {

    fun trackRecordingStarted(source: String) {
        analyticsService.track(
            "recording_started",
            mapOf(
                "source" to source,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackRecordingStopped(
        duration: Long,
        fileSize: Long,
        source: String
    ) {
        analyticsService.track(
            "recording_stopped",
            mapOf(
                "duration_ms" to duration,
                "file_size_bytes" to fileSize,
                "source" to source
            )
        )
    }

    fun trackRecordingError(
        error: String,
        source: String
    ) {
        analyticsService.track(
            "recording_error",
            mapOf(
                "error" to error,
                "source" to source
            )
        )
    }

    fun trackTranscriptionStarted(
        audioLength: Double,
        language: String
    ) {
        analyticsService.track(
            "transcription_started",
            mapOf(
                "audio_length_seconds" to audioLength,
                "language" to language
            )
        )
    }

    fun trackTranscriptionCompleted(
        duration: Long,
        wordCount: Int,
        confidence: Float
    ) {
        analyticsService.track(
            "transcription_completed",
            mapOf(
                "duration_ms" to duration,
                "word_count" to wordCount,
                "confidence" to confidence
            )
        )
    }

    fun trackTranscriptionError(
        error: String,
        audioLength: Double
    ) {
        analyticsService.track(
            "transcription_error",
            mapOf(
                "error" to error,
                "audio_length_seconds" to audioLength
            )
        )
    }

    fun trackSpeakerDetectionStarted(
        audioLength: Double
    ) {
        analyticsService.track(
            "speaker_detection_started",
            mapOf(
                "audio_length_seconds" to audioLength
            )
        )
    }

    fun trackSpeakerDetectionCompleted(
        speakerCount: Int,
        confidence: Float,
        durationMs: Long
    ) {
        analyticsService.track(
            "speaker_detection_completed",
            mapOf(
                "speaker_count" to speakerCount,
                "confidence" to confidence,
                "duration_ms" to durationMs
            )
        )
    }

    fun trackSpeakerDetectionError(
        error: String,
        audioLength: Double
    ) {
        analyticsService.track(
            "speaker_detection_error",
            mapOf(
                "error" to error,
                "audio_length_seconds" to audioLength
            )
        )
    }

    fun trackAudioProcessingStarted(
        source: String,
        durationSeconds: Double,
        format: String
    ) {
        analyticsService.track(
            "audio_processing_started",
            mapOf(
                "source" to source,
                "duration_seconds" to durationSeconds,
                "format" to format
            )
        )
    }

    fun trackAudioProcessingCompleted(
        source: String,
        durationSeconds: Double,
        format: String
    ) {
        analyticsService.track(
            "audio_processing_completed",
            mapOf(
                "source" to source,
                "duration_seconds" to durationSeconds,
                "format" to format
            )
        )
    }

    fun trackAudioProcessingError(
        source: String,
        error: String,
        durationSeconds: Double
    ) {
        analyticsService.track(
            "audio_processing_error",
            mapOf(
                "source" to source,
                "error" to error,
                "duration_seconds" to durationSeconds
            )
        )
    }
} 