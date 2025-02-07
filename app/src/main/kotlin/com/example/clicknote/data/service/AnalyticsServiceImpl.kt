package com.example.clicknote.data.service

import android.content.Context
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.service.AnalyticsService
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mixpanel: MixpanelAPI
) : AnalyticsService {

    override fun track(eventName: String, properties: Map<String, Any>) {
        val jsonObject = JSONObject()
        properties.forEach { (key, value) ->
            jsonObject.put(key, value)
        }
        mixpanel.track(eventName, jsonObject)
    }

    override fun setUserProperty(propertyName: String, value: Any) {
        mixpanel.people.set(propertyName, value)
    }

    override fun identify(userId: String) {
        mixpanel.identify(userId)
    }

    override fun reset() {
        mixpanel.reset()
    }

    override fun trackScreenView(screenName: String) {
        track("screen_view", mapOf("screen_name" to screenName))
    }

    override fun trackError(error: String, screen: String) {
        track("error", mapOf(
            "error_message" to error,
            "screen" to screen
        ))
    }

    override fun trackSearch(query: String, resultCount: Int) {
        track("search", mapOf(
            "query" to query,
            "result_count" to resultCount
        ))
    }

    override fun trackNoteCreated(note: Note) {
        track("note_created", mapOf(
            "note_id" to note.id,
            "source" to note.source.name,
            "has_audio" to note.hasAudio,
            "duration" to (note.duration ?: 0)
        ))
    }

    override fun trackNoteDeleted(note: Note) {
        track("note_deleted", mapOf(
            "note_id" to note.id,
            "source" to note.source.name
        ))
    }

    override fun trackNoteRestored(note: Note) {
        track("note_restored", mapOf(
            "note_id" to note.id,
            "source" to note.source.name
        ))
    }

    override fun trackTranscriptionStarted(source: String) {
        track("transcription_started", mapOf("source" to source))
    }

    override fun trackTranscriptionCompleted(duration: Long, wordCount: Int) {
        track("transcription_completed", mapOf(
            "duration_ms" to duration,
            "word_count" to wordCount
        ))
    }

    override fun trackTranscriptionFailed(error: String) {
        track("transcription_failed", mapOf("error" to error))
    }

    override fun trackSubscriptionChanged(plan: SubscriptionPlan) {
        track("subscription_changed", mapOf(
            "plan_name" to plan.name,
            "plan_price" to plan.price,
            "plan_period" to plan.period.name
        ))
    }

    override fun trackCallRecordingStarted(phoneNumber: String, isIncoming: Boolean) {
        track("call_recording_started", mapOf(
            "phone_number" to phoneNumber,
            "is_incoming" to isIncoming
        ))
    }

    override fun trackCallRecordingCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int,
        isIncoming: Boolean
    ) {
        track("call_recording_completed", mapOf(
            "phone_number" to phoneNumber,
            "duration_ms" to duration,
            "transcription_length" to transcriptionLength,
            "is_incoming" to isIncoming
        ))
    }

    override fun trackCallRecordingError(phoneNumber: String, error: String) {
        track("call_recording_error", mapOf(
            "phone_number" to phoneNumber,
            "error" to error
        ))
    }

    override fun trackStorageUsage(usedBytes: Long, totalBytes: Long) {
        track("storage_usage", mapOf(
            "used_bytes" to usedBytes,
            "total_bytes" to totalBytes,
            "usage_percentage" to (usedBytes.toDouble() / totalBytes * 100)
        ))
    }
} 