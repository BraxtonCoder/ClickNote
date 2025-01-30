package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.service.PremiumFeature
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mixpanel = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN)

    fun trackNoteCreated(note: Note) {
        val properties = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.audioPath != null)
            put("folder_id", note.folderId)
            put("word_count", note.content.split(" ").size)
            put("character_count", note.content.length)
            put("duration", note.duration)
        }
        mixpanel.track("Note Created", properties)
    }

    fun trackNoteDeleted(note: Note) {
        val properties = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.audioPath != null)
            put("folder_id", note.folderId)
            put("age_days", note.createdAt.until(note.updatedAt).toDays())
        }
        mixpanel.track("Note Deleted", properties)
    }

    fun trackNoteRestored(note: Note) {
        val properties = JSONObject().apply {
            put("note_id", note.id)
            put("has_audio", note.audioPath != null)
            put("folder_id", note.folderId)
        }
        mixpanel.track("Note Restored", properties)
    }

    fun trackSubscriptionStarted(plan: SubscriptionPlan) {
        val properties = JSONObject().apply {
            put("plan_type", plan.name)
            put("price", when (plan) {
                SubscriptionPlan.MONTHLY -> 9.99
                SubscriptionPlan.ANNUAL -> 98.00
            })
        }
        mixpanel.track("Subscription Started", properties)
    }

    fun trackSubscriptionCanceled(plan: SubscriptionPlan) {
        val properties = JSONObject().apply {
            put("plan_type", plan.name)
        }
        mixpanel.track("Subscription Canceled", properties)
    }

    fun trackSearch(query: String, resultCount: Int) {
        val properties = JSONObject().apply {
            put("query", query)
            put("result_count", resultCount)
        }
        mixpanel.track("Search Performed", properties)
    }

    fun trackBackupCreated(size: Long, noteCount: Int, audioCount: Int) {
        val properties = JSONObject().apply {
            put("size_mb", size / (1024 * 1024))
            put("note_count", noteCount)
            put("audio_count", audioCount)
        }
        mixpanel.track("Backup Created", properties)
    }

    fun trackBackupRestored(size: Long, noteCount: Int, audioCount: Int) {
        val properties = JSONObject().apply {
            put("size_mb", size / (1024 * 1024))
            put("note_count", noteCount)
            put("audio_count", audioCount)
        }
        mixpanel.track("Backup Restored", properties)
    }

    fun trackError(error: String, screen: String) {
        val properties = JSONObject().apply {
            put("error_message", error)
            put("screen", screen)
        }
        mixpanel.track("Error Occurred", properties)
    }

    fun setUserProperties(userId: String, email: String?) {
        mixpanel.identify(userId)
        val properties = JSONObject().apply {
            put("\$email", email)
        }
        mixpanel.people.set(properties)
    }

    fun clearUserProperties() {
        mixpanel.reset()
    }

    fun trackFeatureAccess(
        feature: String,
        isAllowed: Boolean,
        subscriptionState: Any
    ) {
        val properties = JSONObject().apply {
            put("feature", feature)
            put("is_allowed", isAllowed)
            put("subscription_state", subscriptionState.toString())
            put("timestamp", System.currentTimeMillis())
        }
        mixpanel.track("feature_access_attempt", properties)
    }

    fun trackUpgradePromptShown(
        feature: PremiumFeature,
        remainingCount: Int?,
        source: String
    ) {
        val properties = JSONObject().apply {
            put("feature", feature.name)
            put("remaining_count", remainingCount)
            put("source", source)
            put("timestamp", System.currentTimeMillis())
        }
        mixpanel.track("upgrade_prompt_shown", properties)
    }

    fun trackUpgradePromptAction(
        feature: PremiumFeature,
        action: String,
        source: String
    ) {
        val properties = JSONObject().apply {
            put("feature", feature.name)
            put("action", action) // "upgrade", "dismiss"
            put("source", source)
            put("timestamp", System.currentTimeMillis())
        }
        mixpanel.track("upgrade_prompt_action", properties)
    }

    fun trackWeeklyLimitUpdate(
        newCount: Int,
        remainingCount: Int
    ) {
        val properties = JSONObject().apply {
            put("new_count", newCount)
            put("remaining_count", remainingCount)
            put("timestamp", System.currentTimeMillis())
        }
        mixpanel.track("weekly_limit_update", properties)
    }

    fun trackSubscriptionConversion(
        previousState: String,
        newPlan: SubscriptionPlan,
        source: String,
        triggeringFeature: PremiumFeature?
    ) {
        val properties = JSONObject().apply {
            put("previous_state", previousState)
            put("new_plan", newPlan.name)
            put("source", source)
            put("triggering_feature", triggeringFeature?.name)
            put("timestamp", System.currentTimeMillis())
        }
        mixpanel.track("subscription_conversion", properties)
    }

    companion object {
        private const val MIXPANEL_TOKEN = "YOUR_MIXPANEL_TOKEN"
    }
} 