package com.example.clicknote.analytics

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SubscriptionPlan

interface AnalyticsTracker {
    fun trackScreenView(screenName: String)
    fun trackEvent(eventName: String, properties: Map<String, Any>)
    fun trackError(error: String, screen: String)
    fun trackSearch(query: String, resultCount: Int)
    fun trackSubscriptionChanged(plan: SubscriptionPlan)
    fun trackNoteCreated(note: Note)
    fun trackNoteDeleted(note: Note)
    fun trackNoteRestored(note: Note)
    fun trackTranscriptionStarted(source: String)
    fun trackTranscriptionCompleted(duration: Long, wordCount: Int)
    fun trackTranscriptionFailed(error: String)
    fun setUserProperties(properties: Map<String, Any>)
    fun identify(userId: String)
    fun reset()
    fun trackCallRecordingStarted(phoneNumber: String, isIncoming: Boolean)
    fun trackCallRecordingCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int,
        isIncoming: Boolean
    )
    fun trackCallRecordingError(phoneNumber: String, error: String)
    fun trackStorageUsage(usedBytes: Long, totalBytes: Long)

    // Permission Events
    fun trackPermissionRequested(permission: String)
    fun trackPermissionGranted(permission: String)
    fun trackPermissionDenied(permission: String, isPermanent: Boolean)
    fun trackPermissionReset(permission: String)

    // Speaker Detection Events
    fun trackSpeakerDetectionStarted(audioLengthSeconds: Double)
    fun trackSpeakerDetectionCompleted(
        speakerCount: Int,
        confidence: Float,
        durationMs: Long,
        success: Boolean
    )
    fun trackSpeakerDetectionError(
        errorCode: String,
        errorMessage: String,
        audioLengthSeconds: Double
    )

    // Model Events
    fun trackModelLoaded(modelName: String, loadTimeMs: Long)
    fun trackModelLoadError(modelName: String, errorMessage: String)

    // Audio Processing Events
    fun trackAudioProcessingStarted(
        source: String,
        durationSeconds: Double,
        format: String
    )
    fun trackAudioProcessingCompleted(
        source: String,
        durationSeconds: Double,
        format: String,
        success: Boolean
    )
    fun trackAudioProcessingError(
        source: String,
        errorMessage: String,
        durationSeconds: Double
    )

    // Performance Events
    fun trackPerformanceMetric(
        metricName: String,
        durationMs: Long,
        success: Boolean,
        additionalData: Map<String, Any> = emptyMap()
    )
} 