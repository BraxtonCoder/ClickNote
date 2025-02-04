package com.example.clicknote.analytics

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import com.example.clicknote.BuildConfig

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mixpanel: MixpanelAPI by lazy {
        MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN, false)
    }

    fun trackCallRecordingStarted(phoneNumber: String, isIncoming: Boolean) {
        val properties = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("is_incoming", isIncoming)
        }
        mixpanel.track("call_recording_started", properties)
    }

    fun trackCallRecordingCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int,
        isIncoming: Boolean
    ) {
        val properties = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("duration_ms", duration)
            put("transcription_length", transcriptionLength)
            put("is_incoming", isIncoming)
        }
        mixpanel.track("call_recording_completed", properties)
    }

    fun trackCallRecordingError(phoneNumber: String, error: String) {
        val properties = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("error", error)
        }
        mixpanel.track("call_recording_error", properties)
    }

    fun trackTranscriptionStarted(phoneNumber: String) {
        val properties = JSONObject().apply {
            put("phone_number", phoneNumber)
        }
        mixpanel.track("transcription_started", properties)
    }

    fun trackTranscriptionCompleted(
        phoneNumber: String,
        duration: Long,
        transcriptionLength: Int
    ) {
        val properties = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("duration_ms", duration)
            put("transcription_length", transcriptionLength)
        }
        mixpanel.track("transcription_completed", properties)
    }

    fun trackTranscriptionError(phoneNumber: String, error: String) {
        val properties = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("error", error)
        }
        mixpanel.track("transcription_error", properties)
    }

    fun trackUserSubscriptionChanged(
        oldPlan: String,
        newPlan: String,
        source: String
    ) {
        val properties = JSONObject().apply {
            put("old_plan", oldPlan)
            put("new_plan", newPlan)
            put("source", source)
        }
        mixpanel.track("subscription_changed", properties)
    }

    fun trackUserPreferenceChanged(
        preference: String,
        oldValue: Any?,
        newValue: Any?
    ) {
        val properties = JSONObject().apply {
            put("preference", preference)
            put("old_value", oldValue.toString())
            put("new_value", newValue.toString())
        }
        mixpanel.track("preference_changed", properties)
    }

    fun setUserProfile(
        userId: String,
        email: String?,
        subscriptionType: String,
        isFirstLaunch: Boolean
    ) {
        mixpanel.identify(userId)
        mixpanel.people.set("email", email)
        mixpanel.people.set("subscription_type", subscriptionType)
        mixpanel.people.set("first_launch", isFirstLaunch)
    }

    fun trackCloudUploadStarted(fileType: String, fileSize: Long) {
        val properties = JSONObject().apply {
            put("file_type", fileType)
            put("file_size", fileSize)
        }
        mixpanel.track("cloud_upload_started", properties)
    }

    fun trackCloudUploadCompleted(fileType: String, fileSize: Long, duration: Long) {
        val properties = JSONObject().apply {
            put("file_type", fileType)
            put("file_size", fileSize)
            put("duration_ms", duration)
        }
        mixpanel.track("cloud_upload_completed", properties)
    }

    fun trackCloudUploadError(error: String, fileType: String) {
        val properties = JSONObject().apply {
            put("error", error)
            put("file_type", fileType)
        }
        mixpanel.track("cloud_upload_error", properties)
    }

    fun trackCloudDownloadStarted(fileType: String) {
        val properties = JSONObject().apply {
            put("file_type", fileType)
        }
        mixpanel.track("cloud_download_started", properties)
    }

    fun trackCloudDownloadCompleted(fileType: String, fileSize: Long, duration: Long) {
        val properties = JSONObject().apply {
            put("file_type", fileType)
            put("file_size", fileSize)
            put("duration_ms", duration)
        }
        mixpanel.track("cloud_download_completed", properties)
    }

    fun trackCloudDownloadError(error: String, fileType: String) {
        val properties = JSONObject().apply {
            put("error", error)
            put("file_type", fileType)
        }
        mixpanel.track("cloud_download_error", properties)
    }

    fun trackCloudDeletionCompleted(fileType: String) {
        val properties = JSONObject().apply {
            put("file_type", fileType)
        }
        mixpanel.track("cloud_deletion_completed", properties)
    }

    fun trackCloudDeletionError(error: String, fileType: String) {
        val properties = JSONObject().apply {
            put("error", error)
            put("file_type", fileType)
        }
        mixpanel.track("cloud_deletion_error", properties)
    }

    fun trackCloudSyncStarted(itemCount: Int) {
        val properties = JSONObject().apply {
            put("item_count", itemCount)
        }
        mixpanel.track("cloud_sync_started", properties)
    }

    fun trackCloudSyncCompleted(itemCount: Int) {
        val properties = JSONObject().apply {
            put("item_count", itemCount)
        }
        mixpanel.track("cloud_sync_completed", properties)
    }

    fun trackCloudSyncError(error: String) {
        val properties = JSONObject().apply {
            put("error", error)
        }
        mixpanel.track("cloud_sync_error", properties)
    }

    companion object {
        // Remove the hardcoded token since we're now using BuildConfig
    }
} 