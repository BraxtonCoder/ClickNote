package com.example.clicknote.analytics

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SubscriptionPlan

abstract class BaseAnalyticsTracker : AnalyticsTracker {
    override fun trackScreenView(screenName: String) {}
    override fun trackEvent(eventName: String, properties: Map<String, Any>) {}
    override fun trackError(error: String, screen: String) {}
    override fun trackSearch(query: String, resultCount: Int) {}
    override fun trackSubscriptionChanged(plan: SubscriptionPlan) {}
    override fun trackNoteCreated(note: Note) {}
    override fun trackNoteDeleted(note: Note) {}
    override fun trackNoteRestored(note: Note) {}
    override fun trackTranscriptionStarted(source: String) {}
    override fun trackTranscriptionCompleted(duration: Long, wordCount: Int) {}
    override fun trackTranscriptionFailed(error: String) {}
    override fun setUserProperties(properties: Map<String, Any>) {}
    override fun identify(userId: String) {}
    override fun reset() {}
    override fun trackCallRecordingStarted(phoneNumber: String, isIncoming: Boolean) {}
    override fun trackCallRecordingCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int,
        isIncoming: Boolean
    ) {}
    override fun trackCallRecordingError(phoneNumber: String, error: String) {}
    override fun trackStorageUsage(usedBytes: Long, totalBytes: Long) {}

    override fun trackPermissionRequested(permission: String) {}
    override fun trackPermissionGranted(permission: String) {}
    override fun trackPermissionDenied(permission: String, isPermanent: Boolean) {}
    override fun trackPermissionReset(permission: String) {}

    override fun trackSpeakerDetectionStarted(audioLengthSeconds: Double) {}
    override fun trackSpeakerDetectionCompleted(
        speakerCount: Int,
        confidence: Float,
        durationMs: Long,
        success: Boolean
    ) {}
    override fun trackSpeakerDetectionError(
        errorCode: String,
        errorMessage: String,
        audioLengthSeconds: Double
    ) {}

    override fun trackModelLoaded(modelName: String, loadTimeMs: Long) {}
    override fun trackModelLoadError(modelName: String, errorMessage: String) {}

    override fun trackAudioProcessingStarted(
        source: String,
        durationSeconds: Double,
        format: String
    ) {}
    override fun trackAudioProcessingCompleted(
        source: String,
        durationSeconds: Double,
        format: String,
        success: Boolean
    ) {}
    override fun trackAudioProcessingError(
        source: String,
        errorMessage: String,
        durationSeconds: Double
    ) {}

    override fun trackPerformanceMetric(
        metricName: String,
        durationMs: Long,
        success: Boolean,
        additionalData: Map<String, Any>
    ) {}
} 