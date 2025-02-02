package com.example.clicknote.service.analytics

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mixpanel: MixpanelAPI by lazy {
        MixpanelAPI.getInstance(context, MIXPANEL_TOKEN, true)
    }

    suspend fun trackEvent(event: AnalyticsEvent) = withContext(Dispatchers.IO) {
        val properties = JSONObject().apply {
            event.properties.forEach { (key, value) ->
                put(key, value)
            }
        }
        mixpanel.track(event.name, properties)
    }

    fun setUserProperties(properties: Map<String, Any>) {
        val jsonProperties = JSONObject()
        properties.forEach { (key, value) ->
            jsonProperties.put(key, value)
        }
        mixpanel.people.set(jsonProperties)
    }

    fun identify(userId: String) {
        mixpanel.identify(userId)
    }

    fun reset() {
        mixpanel.reset()
    }

    companion object {
        private const val MIXPANEL_TOKEN = "YOUR_MIXPANEL_TOKEN" // Replace with actual token
    }
}

sealed class AnalyticsEvent(
    val name: String,
    val properties: Map<String, Any> = emptyMap()
) {
    // User Events
    data class UserSignedIn(val method: String) : AnalyticsEvent(
        name = "user_signed_in",
        properties = mapOf("method" to method)
    )
    
    data class UserSignedOut : AnalyticsEvent("user_signed_out")
    
    data class SubscriptionPurchased(val plan: String, val price: Double) : AnalyticsEvent(
        name = "subscription_purchased",
        properties = mapOf(
            "plan" to plan,
            "price" to price
        )
    )

    // Note Events
    data class NoteCreated(
        val source: String,
        val hasAudio: Boolean,
        val length: Int,
        val offline: Boolean
    ) : AnalyticsEvent(
        name = "note_created",
        properties = mapOf(
            "source" to source,
            "has_audio" to hasAudio,
            "length" to length,
            "offline" to offline
        )
    )

    data class NoteDeleted(val source: String) : AnalyticsEvent(
        name = "note_deleted",
        properties = mapOf("source" to source)
    )

    data class NoteMoved(val source: String, val destination: String) : AnalyticsEvent(
        name = "note_moved",
        properties = mapOf(
            "source" to source,
            "destination" to destination
        )
    )

    // Feature Usage Events
    data class SummaryGenerated(
        val templateId: String,
        val templateCategory: String,
        val noteLength: Int,
        val success: Boolean,
        val errorType: String? = null
    ) : AnalyticsEvent(
        name = "summary_generated",
        properties = buildMap {
            put("template_id", templateId)
            put("template_category", templateCategory)
            put("note_length", noteLength)
            put("success", success)
            errorType?.let { put("error_type", it) }
        }
    )

    data class SearchPerformed(
        val queryLength: Int,
        val filters: List<String>,
        val resultCount: Int
    ) : AnalyticsEvent(
        name = "search_performed",
        properties = mapOf(
            "query_length" to queryLength,
            "filters" to filters,
            "result_count" to resultCount
        )
    )

    data class ErrorOccurred(
        val type: String,
        val message: String,
        val screen: String,
        val stackTrace: String? = null
    ) : AnalyticsEvent(
        name = "error_occurred",
        properties = buildMap {
            put("type", type)
            put("message", message)
            put("screen", screen)
            stackTrace?.let { put("stack_trace", it) }
        }
    )

    // Performance Events
    data class TranscriptionPerformed(
        val duration: Long,
        val success: Boolean,
        val offline: Boolean,
        val errorType: String? = null
    ) : AnalyticsEvent(
        name = "transcription_performed",
        properties = buildMap {
            put("duration_ms", duration)
            put("success", success)
            put("offline", offline)
            errorType?.let { put("error_type", it) }
        }
    )

    data class AppStartup(
        val duration: Long,
        val coldStart: Boolean
    ) : AnalyticsEvent(
        name = "app_startup",
        properties = mapOf(
            "duration_ms" to duration,
            "cold_start" to coldStart
        )
    )
} 