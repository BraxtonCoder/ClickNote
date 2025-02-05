package com.example.clicknote.data.service

import android.content.Context
import com.example.clicknote.BuildConfig
import com.example.clicknote.domain.model.Note
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
        true // Enable opt-out tracking
    )

    override fun trackScreenView(screenName: String) {
        val props = JSONObject().apply {
            put("screen_name", screenName)
        }
        mixpanel.trackMap("screen_view", props.toMap())
    }

    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        mixpanel.trackMap(eventName, properties)
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

    override fun trackNoteCreated(note: Note) {
        val props = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.hasAudio)
            put("is_long_form", note.isLongForm)
            put("word_count", note.content.split(" ").size)
            put("folder_id", note.folderId ?: "none")
        }
        mixpanel.trackMap("note_created", props.toMap())
    }

    override fun trackNoteDeleted(note: Note) {
        val props = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.hasAudio)
            put("is_long_form", note.isLongForm)
        }
        mixpanel.trackMap("note_deleted", props.toMap())
    }

    override fun trackNoteRestored(note: Note) {
        val props = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.hasAudio)
            put("is_long_form", note.isLongForm)
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
        mixpanel.getPeople().apply {
            properties.forEach { (key, value) ->
                when (value) {
                    is String -> set(key, value)
                    is Number -> set(key, value)
                    is Boolean -> set(key, value)
                    else -> set(key, value.toString())
                }
            }
        }
    }

    override fun identify(userId: String) {
        mixpanel.identify(userId)
        mixpanel.getPeople().identify(userId)
    }

    override fun reset() {
        mixpanel.optOutTracking()
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