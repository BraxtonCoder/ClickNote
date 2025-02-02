package com.example.clicknote.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.clicknote.domain.model.CloudProvider
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {

    private object PreferencesKeys {
        val CALL_RECORDING_ENABLED = booleanPreferencesKey("call_recording_enabled")
        val AUDIO_SAVING_ENABLED = booleanPreferencesKey("audio_saving_enabled")
        val OFFLINE_TRANSCRIPTION_ENABLED = booleanPreferencesKey("offline_transcription_enabled")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val WEEKLY_TRANSCRIPTION_COUNT = intPreferencesKey("weekly_transcription_count")
        val DETECT_SPEAKERS = booleanPreferencesKey("detect_speakers")
        val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("transcription_language")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val CLOUD_PROVIDER = stringPreferencesKey("cloud_provider")
        val BUTTON_TRIGGER_DELAY = longPreferencesKey("button_trigger_delay")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val SHOW_SILENT_NOTIFICATIONS = booleanPreferencesKey("show_silent_notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_FIRST_TIME_USER = booleanPreferencesKey("is_first_time_user")
        val LAST_TRANSCRIPTION_RESET = longPreferencesKey("last_transcription_reset")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        val BACKUP_WIFI_ONLY = booleanPreferencesKey("backup_wifi_only")
        val BACKUP_AUDIO = booleanPreferencesKey("backup_audio")
        val BACKUP_TRANSCRIPTIONS = booleanPreferencesKey("backup_transcriptions")
        val BACKUP_SETTINGS = booleanPreferencesKey("backup_settings")
        val MAX_BACKUP_SIZE = longPreferencesKey("max_backup_size")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    }

    override val callRecordingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.CALL_RECORDING_ENABLED] ?: false
        }

    override val audioSavingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUDIO_SAVING_ENABLED] ?: true
        }

    override val offlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.OFFLINE_TRANSCRIPTION_ENABLED] ?: false
        }

    override val selectedLanguage: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"
        }

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    override val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
        }

    override val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    override val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
        }

    override val lastSyncTime: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] ?: 0L
        }

    override val cloudSyncEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] ?: false
        }

    override val weeklyTranscriptionCount: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
        }

    override val detectSpeakers: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DETECT_SPEAKERS] ?: false
        }

    override val transcriptionLanguage: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] ?: "en"
        }

    override val audioQuality: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] ?: "high"
        }

    override val cloudProvider: Flow<CloudProvider> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[PreferencesKeys.CLOUD_PROVIDER] ?: "none") {
                "none" -> CloudProvider.NONE
                "google_cloud" -> CloudProvider.GOOGLE_CLOUD
                "local_cloud" -> CloudProvider.LOCAL_CLOUD
                else -> CloudProvider.NONE
            }
        }

    override val buttonTriggerDelay: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUTTON_TRIGGER_DELAY] ?: 750L
        }

    override val openaiApiKey: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.OPENAI_API_KEY] ?: ""
        }

    override val showSilentNotifications: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_SILENT_NOTIFICATIONS] ?: true
        }

    override val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    override val isFirstTimeUser: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_FIRST_TIME_USER] ?: true
        }

    override val lastTranscriptionReset: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TRANSCRIPTION_RESET] ?: 0L
        }

    override suspend fun setCallRecordingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALL_RECORDING_ENABLED] = enabled
        }
    }

    override suspend fun setAudioSavingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_SAVING_ENABLED] = enabled
        }
    }

    override suspend fun setOfflineTranscriptionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_TRANSCRIPTION_ENABLED] = enabled
        }
    }

    override suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = language
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    override suspend fun setIsFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = isFirst
        }
    }

    override suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = time
        }
    }

    override suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] = enabled
        }
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = currentCount + 1
        }
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = 0
        }
    }

    override suspend fun setDetectSpeakers(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DETECT_SPEAKERS] = enabled
        }
    }

    override suspend fun setTranscriptionLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] = language
        }
    }

    override suspend fun setAudioQuality(quality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality
        }
    }

    override suspend fun setCloudProvider(provider: CloudProvider) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_PROVIDER] = when (provider) {
                CloudProvider.NONE -> "none"
                CloudProvider.GOOGLE_CLOUD -> "google_cloud"
                CloudProvider.LOCAL_CLOUD -> "local_cloud"
            }
        }
    }

    override suspend fun setButtonTriggerDelay(delay: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUTTON_TRIGGER_DELAY] = delay
        }
    }

    override suspend fun setOpenaiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OPENAI_API_KEY] = key
        }
    }

    override suspend fun setShowSilentNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SILENT_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setIsFirstTimeUser(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_TIME_USER] = isFirst
        }
    }

    override suspend fun setLastTranscriptionReset(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TRANSCRIPTION_RESET] = time
        }
    }
} 