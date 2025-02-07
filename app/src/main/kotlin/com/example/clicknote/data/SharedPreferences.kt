package com.example.clicknote.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.clicknote.domain.model.SpeakerProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveSpeakerProfile(profile: SpeakerProfile) {
        prefs.edit {
            putString(KEY_SPEAKER_PROFILE + profile.id, gson.toJson(profile))
        }
    }

    fun getSpeakerProfile(id: String): SpeakerProfile? {
        val json = prefs.getString(KEY_SPEAKER_PROFILE + id, null)
        return json?.let {
            gson.fromJson(it, SpeakerProfile::class.java)
        }
    }

    fun getAllSpeakerProfiles(): List<SpeakerProfile> {
        return prefs.all
            .filterKeys { it.startsWith(KEY_SPEAKER_PROFILE) }
            .mapNotNull { (_, value) ->
                try {
                    gson.fromJson(value as String, SpeakerProfile::class.java)
                } catch (e: Exception) {
                    null
                }
            }
    }

    fun deleteSpeakerProfile(id: String) {
        prefs.edit {
            remove(KEY_SPEAKER_PROFILE + id)
        }
    }

    fun clearAllSpeakerProfiles() {
        prefs.edit {
            prefs.all.keys
                .filter { it.startsWith(KEY_SPEAKER_PROFILE) }
                .forEach { remove(it) }
        }
    }

    // Transcription settings
    var weeklyTranscriptionCount: Int
        get() = prefs.getInt(KEY_WEEKLY_TRANSCRIPTION_COUNT, 0)
        set(value) = prefs.edit { putInt(KEY_WEEKLY_TRANSCRIPTION_COUNT, value) }

    fun incrementWeeklyTranscriptionCount() {
        weeklyTranscriptionCount = weeklyTranscriptionCount + 1
    }

    fun resetWeeklyTranscriptionCount() {
        weeklyTranscriptionCount = 0
    }

    // Feature flags and settings
    var callRecordingEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALL_RECORDING_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_CALL_RECORDING_ENABLED, value) }

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_VIBRATION_ENABLED, value) }

    var buttonTriggerDelay: Long
        get() = prefs.getLong(KEY_BUTTON_TRIGGER_DELAY, 750L)
        set(value) = prefs.edit { putLong(KEY_BUTTON_TRIGGER_DELAY, value) }

    var audioSavingEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUDIO_SAVING_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_AUDIO_SAVING_ENABLED, value) }

    var showSilentNotifications: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SILENT_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_SILENT_NOTIFICATIONS, value) }

    var cloudSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_CLOUD_SYNC_ENABLED, value) }

    var cloudProvider: String
        get() = prefs.getString(KEY_CLOUD_PROVIDER, "LOCAL") ?: "LOCAL"
        set(value) = prefs.edit { putString(KEY_CLOUD_PROVIDER, value) }

    companion object {
        private const val PREFS_NAME = "ClickNotePrefs"
        private const val KEY_SPEAKER_PROFILE = "speaker_profile_"
        private const val KEY_WEEKLY_TRANSCRIPTION_COUNT = "weekly_transcription_count"
        private const val KEY_CALL_RECORDING_ENABLED = "call_recording_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_BUTTON_TRIGGER_DELAY = "button_trigger_delay"
        private const val KEY_AUDIO_SAVING_ENABLED = "audio_saving_enabled"
        private const val KEY_SHOW_SILENT_NOTIFICATIONS = "show_silent_notifications"
        private const val KEY_CLOUD_SYNC_ENABLED = "cloud_sync_enabled"
        private const val KEY_CLOUD_PROVIDER = "cloud_provider"
    }
} 