package com.example.clicknote.domain.service

interface AnalyticsService {
    fun logEvent(event: AnalyticsEvent, properties: Map<String, Any> = emptyMap())
    fun setUserProperty(property: String, value: Any)
    fun setUserId(userId: String)
    fun startSession()
    fun endSession()
    fun enableTracking(enabled: Boolean)
    fun isTrackingEnabled(): Boolean
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