package com.example.clicknote.data.service

import android.content.Context
import com.example.clicknote.BuildConfig
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
    @ApplicationContext context: Context
) : AnalyticsService {

    private val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(
        context,
        BuildConfig.MIXPANEL_TOKEN,
        true
    )

    override fun trackScreenView(screenName: String) {
        val props = JSONObject().apply {
            put("screen_name", screenName)
        }
        mixpanel.trackMap("screen_view", props.toMap())
    }

    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        val props = JSONObject()
        properties.forEach { (key, value) ->
            when (value) {
                is String -> props.put(key, value)
                is Number -> props.put(key, value)
                is Boolean -> props.put(key, value)
                else -> props.put(key, value.toString())
            }
        }
        mixpanel.trackMap(eventName, props.toMap())
    }

    override fun trackError(error: String, screen: String) {
        val props = JSONObject().apply {
            put("error_message", error)
            put("screen", screen)
        }
        mixpanel.trackMap("error", props.toMap())
    }

    override fun trackSearch(query: String, resultCount: Int) {
        val props = JSONObject().apply {
            put("query", query)
            put("result_count", resultCount)
        }
        mixpanel.trackMap("search", props.toMap())
    }

    override fun trackSubscriptionChanged(plan: SubscriptionPlan) {
        val props = JSONObject().apply {
            put("plan_type", when (plan) {
                is SubscriptionPlan.Monthly -> "MONTHLY"
                is SubscriptionPlan.Annual -> "ANNUAL"
                is SubscriptionPlan.Free -> "FREE"
            })
            put("price", plan.price)
        }
        mixpanel.trackMap("subscription_changed", props.toMap())
    }

    override fun trackNoteCreated(note: Note) {
        val props = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.hasAudio)
            put("word_count", note.content.split(" ").size)
            put("folder_id", note.folderId ?: "none")
        }
        mixpanel.trackMap("note_created", props.toMap())
    }

    override fun trackNoteDeleted(note: Note) {
        val props = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.hasAudio)
        }
        mixpanel.trackMap("note_deleted", props.toMap())
    }

    override fun trackNoteRestored(note: Note) {
        val props = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.hasAudio)
        }
        mixpanel.trackMap("note_restored", props.toMap())
    }

    override fun trackTranscriptionStarted(source: String) {
        val props = JSONObject().apply {
            put("source", source)
        }
        mixpanel.trackMap("transcription_started", props.toMap())
    }

    override fun trackTranscriptionCompleted(duration: Long, wordCount: Int) {
        val props = JSONObject().apply {
            put("duration_ms", duration)
            put("word_count", wordCount)
        }
        mixpanel.trackMap("transcription_completed", props.toMap())
    }

    override fun trackTranscriptionFailed(error: String) {
        val props = JSONObject().apply {
            put("error_message", error)
        }
        mixpanel.trackMap("transcription_failed", props.toMap())
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        properties.forEach { (key, value) ->
            mixpanel.people.set(key, value)
        }
    }

    override fun identify(userId: String) {
        mixpanel.identify(userId)
        mixpanel.people.identify(userId)
    }

    override fun reset() {
        mixpanel.reset()
    }

    override fun trackCallRecordingStarted(phoneNumber: String, isIncoming: Boolean) {
        val props = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("is_incoming", isIncoming)
            put("timestamp", System.currentTimeMillis())
        }
        mixpanel.trackMap("call_recording_started", props.toMap())
    }

    override fun trackCallRecordingCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int,
        isIncoming: Boolean
    ) {
        val props = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("duration_ms", duration)
            put("transcription_length", transcriptionLength)
            put("is_incoming", isIncoming)
        }
        mixpanel.trackMap("call_recording_completed", props.toMap())
    }

    override fun trackCallRecordingError(phoneNumber: String, error: String) {
        val props = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("error_message", error)
        }
        mixpanel.trackMap("call_recording_error", props.toMap())
    }

    override fun trackStorageUsage(usedBytes: Long, totalBytes: Long) {
        val props = JSONObject().apply {
            put("used_bytes", usedBytes)
            put("total_bytes", totalBytes)
            put("usage_percentage", (usedBytes.toDouble() / totalBytes.toDouble()) * 100)
        }
        mixpanel.trackMap("storage_usage", props.toMap())
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = this.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = this.get(key)
        }
        return map
    }
} 