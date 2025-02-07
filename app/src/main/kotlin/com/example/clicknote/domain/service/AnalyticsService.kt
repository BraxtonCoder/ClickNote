package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SubscriptionPlan

interface AnalyticsService {
    fun track(
        eventName: String,
        properties: Map<String, Any> = emptyMap()
    )

    fun setUserProperty(
        propertyName: String,
        value: Any
    )

    fun identify(userId: String)

    fun reset()

    fun trackScreenView(screenName: String)
    fun trackError(error: String, screen: String)
    fun trackSearch(query: String, resultCount: Int)
    fun trackNoteCreated(note: Note)
    fun trackNoteDeleted(note: Note)
    fun trackNoteRestored(note: Note)
    fun trackTranscriptionStarted(source: String)
    fun trackTranscriptionCompleted(duration: Long, wordCount: Int)
    fun trackTranscriptionFailed(error: String)
    fun trackSubscriptionChanged(plan: SubscriptionPlan)
    fun trackCallRecordingStarted(phoneNumber: String, isIncoming: Boolean)
    fun trackCallRecordingCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int,
        isIncoming: Boolean
    )
    fun trackCallRecordingError(phoneNumber: String, error: String)
    fun trackStorageUsage(usedBytes: Long, totalBytes: Long)
}

sealed class AnalyticsEvent(val name: String) {
    object NoteCreated : AnalyticsEvent("note_created")
    object NoteDeleted : AnalyticsEvent("note_deleted")
    object NoteEdited : AnalyticsEvent("note_edited")
    object RecordingStarted : AnalyticsEvent("recording_started")
    object RecordingStopped : AnalyticsEvent("recording_stopped")
    object TranscriptionStarted : AnalyticsEvent("transcription_started")
    object TranscriptionCompleted : AnalyticsEvent("transcription_completed")
    object SubscriptionPurchased : AnalyticsEvent("subscription_purchased")
    object SubscriptionCancelled : AnalyticsEvent("subscription_cancelled")
    object UserSignedIn : AnalyticsEvent("user_signed_in")
    object UserSignedOut : AnalyticsEvent("user_signed_out")
    class Custom(eventName: String) : AnalyticsEvent(eventName)
} 