package com.example.clicknote.data.analytics

import com.example.clicknote.domain.analytics.AnalyticsService
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MixPanelAnalyticsService @Inject constructor(
    private val mixpanel: MixpanelAPI
) : AnalyticsService {

    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        val jsonProperties = JSONObject(properties)
        mixpanel.track(eventName, jsonProperties)
    }

    override fun setUserProfile(userId: String, properties: Map<String, Any>) {
        val people = mixpanel.people
        properties.forEach { (key, value) ->
            when (value) {
                is String -> people.set(key, value)
                is Number -> people.set(key, value)
                is Boolean -> people.set(key, value)
                is List<*> -> people.set(key, JSONObject().put("value", value))
                else -> people.set(key, value.toString())
            }
        }
    }

    override fun identify(userId: String) {
        mixpanel.identify(userId)
        mixpanel.people.identify(userId)
    }

    override fun reset() {
        mixpanel.reset()
    }

    companion object {
        // Event Names
        const val EVENT_APP_OPENED = "App Opened"
        const val EVENT_NOTE_CREATED = "Note Created"
        const val EVENT_NOTE_DELETED = "Note Deleted"
        const val EVENT_NOTE_EDITED = "Note Edited"
        const val EVENT_TRANSCRIPTION_STARTED = "Transcription Started"
        const val EVENT_TRANSCRIPTION_COMPLETED = "Transcription Completed"
        const val EVENT_TRANSCRIPTION_FAILED = "Transcription Failed"
        const val EVENT_FOLDER_CREATED = "Folder Created"
        const val EVENT_USER_SIGNED_IN = "User Signed In"
        const val EVENT_SUBSCRIPTION_STARTED = "Subscription Started"
        const val EVENT_SUBSCRIPTION_CANCELLED = "Subscription Cancelled"
        
        // Property Names
        const val PROP_NOTE_TYPE = "note_type"
        const val PROP_NOTE_LENGTH = "note_length_seconds"
        const val PROP_TRANSCRIPTION_TYPE = "transcription_type"
        const val PROP_ERROR_MESSAGE = "error_message"
        const val PROP_SUBSCRIPTION_PLAN = "subscription_plan"
        const val PROP_USER_ID = "user_id"
        const val PROP_FOLDER_NAME = "folder_name"
    }
} 