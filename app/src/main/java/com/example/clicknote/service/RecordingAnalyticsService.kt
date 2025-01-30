package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.Note
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
        val properties = JSONObject().apply {
            put("source", source) // "accessibility_button", "app_button", etc.
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Recording Started", properties)
    }

    fun trackRecordingPaused(duration: Long) {
        val properties = JSONObject().apply {
            put("duration_seconds", duration / 1000)
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Recording Paused", properties)
    }

    fun trackRecordingResumed() {
        val properties = JSONObject().apply {
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Recording Resumed", properties)
    }

    fun trackRecordingCompleted(duration: Long, fileSize: Long, transcriptionEnabled: Boolean) {
        val properties = JSONObject().apply {
            put("duration_seconds", duration / 1000)
            put("file_size_mb", fileSize / (1024 * 1024))
            put("transcription_enabled", transcriptionEnabled)
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Recording Completed", properties)
    }

    fun trackRecordingCanceled(duration: Long) {
        val properties = JSONObject().apply {
            put("duration_seconds", duration / 1000)
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Recording Canceled", properties)
    }

    fun trackRecordingError(error: String, phase: String) {
        val properties = JSONObject().apply {
            put("error_message", error)
            put("phase", phase)
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Recording Error", properties)
    }

    fun trackTranscriptionStarted(duration: Long, fileSize: Long) {
        val properties = JSONObject().apply {
            put("duration_seconds", duration / 1000)
            put("file_size_mb", fileSize / (1024 * 1024))
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Transcription Started", properties)
    }

    fun trackTranscriptionCompleted(duration: Long, wordCount: Int, speakerCount: Int) {
        val properties = JSONObject().apply {
            put("duration_seconds", duration / 1000)
            put("word_count", wordCount)
            put("speaker_count", speakerCount)
            put("words_per_minute", (wordCount.toFloat() / (duration / 60000)).toInt())
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Transcription Completed", properties)
    }

    fun trackTranscriptionError(error: String, phase: String) {
        val properties = JSONObject().apply {
            put("error_message", error)
            put("phase", phase)
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Transcription Error", properties)
    }

    fun trackAudioEnhancementStarted(fileSize: Long) {
        val properties = JSONObject().apply {
            put("file_size_mb", fileSize / (1024 * 1024))
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Audio Enhancement Started", properties)
    }

    fun trackAudioEnhancementCompleted(duration: Long, originalSize: Long, enhancedSize: Long) {
        val properties = JSONObject().apply {
            put("duration_seconds", duration / 1000)
            put("original_size_mb", originalSize / (1024 * 1024))
            put("enhanced_size_mb", enhancedSize / (1024 * 1024))
            put("size_reduction_percent", ((originalSize - enhancedSize) * 100 / originalSize))
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Audio Enhancement Completed", properties)
    }

    fun trackAudioEnhancementError(error: String) {
        val properties = JSONObject().apply {
            put("error_message", error)
            put("timestamp", LocalDateTime.now().toString())
        }
        analyticsService.track("Audio Enhancement Error", properties)
    }
} 