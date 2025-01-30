package com.example.clicknote.analytics

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mixpanel: MixpanelAPI by lazy {
        MixpanelAPI.getInstance(context, MIXPANEL_TOKEN)
    }

    // Permission Events
    fun trackPermissionRequested(permission: String) {
        track("Permission Requested") {
            put("permission", permission)
        }
    }

    fun trackPermissionGranted(permission: String) {
        track("Permission Granted") {
            put("permission", permission)
        }
    }

    fun trackPermissionDenied(permission: String, isPermanent: Boolean) {
        track("Permission Denied") {
            put("permission", permission)
            put("is_permanent", isPermanent)
        }
    }

    fun trackPermissionReset(permission: String) {
        track("Permission Reset") {
            put("permission", permission)
        }
    }

    // Speaker Detection Events
    fun trackSpeakerDetectionStarted(audioLengthSeconds: Double) {
        track("Speaker Detection Started") {
            put("audio_length_seconds", audioLengthSeconds)
        }
    }

    fun trackSpeakerDetectionCompleted(
        speakerCount: Int,
        confidence: Float,
        durationMs: Long,
        success: Boolean
    ) {
        track("Speaker Detection Completed") {
            put("speaker_count", speakerCount)
            put("confidence", confidence)
            put("duration_ms", durationMs)
            put("success", success)
        }
    }

    fun trackSpeakerDetectionError(
        errorCode: String,
        errorMessage: String,
        audioLengthSeconds: Double
    ) {
        track("Speaker Detection Error") {
            put("error_code", errorCode)
            put("error_message", errorMessage)
            put("audio_length_seconds", audioLengthSeconds)
        }
    }

    // Model Events
    fun trackModelLoaded(modelName: String, loadTimeMs: Long) {
        track("Model Loaded") {
            put("model_name", modelName)
            put("load_time_ms", loadTimeMs)
        }
    }

    fun trackModelLoadError(modelName: String, errorMessage: String) {
        track("Model Load Error") {
            put("model_name", modelName)
            put("error_message", errorMessage)
        }
    }

    // Audio Processing Events
    fun trackAudioProcessingStarted(
        audioLengthSeconds: Double,
        sampleRate: Int,
        channels: Int
    ) {
        track("Audio Processing Started") {
            put("audio_length_seconds", audioLengthSeconds)
            put("sample_rate", sampleRate)
            put("channels", channels)
        }
    }

    fun trackAudioProcessingCompleted(
        audioLengthSeconds: Double,
        processingTimeMs: Long,
        segmentCount: Int
    ) {
        track("Audio Processing Completed") {
            put("audio_length_seconds", audioLengthSeconds)
            put("processing_time_ms", processingTimeMs)
            put("segment_count", segmentCount)
        }
    }

    fun trackAudioProcessingError(
        errorCode: String,
        errorMessage: String,
        audioLengthSeconds: Double
    ) {
        track("Audio Processing Error") {
            put("error_code", errorCode)
            put("error_message", errorMessage)
            put("audio_length_seconds", audioLengthSeconds)
        }
    }

    // Performance Events
    fun trackPerformanceMetric(
        metricName: String,
        durationMs: Long,
        success: Boolean,
        additionalData: Map<String, Any>? = null
    ) {
        track("Performance Metric") {
            put("metric_name", metricName)
            put("duration_ms", durationMs)
            put("success", success)
            additionalData?.forEach { (key, value) ->
                put(key, value)
            }
        }
    }

    private fun track(eventName: String, properties: JSONObject.() -> Unit) {
        try {
            val propertiesJson = JSONObject().apply(properties)
            mixpanel.track(eventName, propertiesJson)
        } catch (e: Exception) {
            // Log analytics error but don't crash the app
            e.printStackTrace()
        }
    }

    fun setUserProperty(property: String, value: Any) {
        try {
            mixpanel.people.set(property, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val MIXPANEL_TOKEN = "YOUR_MIXPANEL_TOKEN" // Replace with your actual token
    }
} 