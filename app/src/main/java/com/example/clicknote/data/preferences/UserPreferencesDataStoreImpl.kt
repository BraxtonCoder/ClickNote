package com.example.clicknote.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
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
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
        val SUBSCRIPTION_EXPIRY_DATE = longPreferencesKey("subscription_expiry_date")
        val DETECT_SPEAKERS = booleanPreferencesKey("detect_speakers")
        val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("transcription_language")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val CLOUD_PROVIDER = stringPreferencesKey("cloud_provider")
        val BUTTON_TRIGGER_DELAY = longPreferencesKey("button_trigger_delay")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val SHOW_SILENT_NOTIFICATIONS = booleanPreferencesKey("show_silent_notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val callRecordingEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.CALL_RECORDING_ENABLED] ?: false }

    override val audioSavingEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.AUDIO_SAVING_ENABLED] ?: true }

    override val offlineTranscriptionEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.OFFLINE_TRANSCRIPTION_ENABLED] ?: false }

    override val selectedLanguage: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "en" }

    override val notificationsEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    override val vibrationEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true }

    override val themeMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.THEME_MODE] ?: "system" }

    override val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true }

    override val lastSyncTime: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.LAST_SYNC_TIME] ?: 0L }

    override val cloudSyncEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] ?: false }

    override val weeklyTranscriptionCount: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0 }

    override val isPremium: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.IS_PREMIUM] ?: false }

    override val subscriptionType: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.SUBSCRIPTION_TYPE] ?: "" }

    override val subscriptionExpiryDate: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.SUBSCRIPTION_EXPIRY_DATE] ?: 0L }

    override val detectSpeakers: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.DETECT_SPEAKERS] ?: false }

    override val transcriptionLanguage: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] ?: "en" }

    override val audioQuality: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.AUDIO_QUALITY] ?: "HIGH" }

    override val cloudProvider: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.CLOUD_PROVIDER] ?: "AWS" }

    override val buttonTriggerDelay: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.BUTTON_TRIGGER_DELAY] ?: 750L }

    override val openaiApiKey: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.OPENAI_API_KEY] ?: "" }

    override val showSilentNotifications: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.SHOW_SILENT_NOTIFICATIONS] ?: true }

    override val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }

    override suspend fun setCallRecordingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALL_RECORDING_ENABLED] = enabled
        }
    }

    override suspend fun setAudioSavingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_SAVING_ENABLED] = enabled
        }
    }

    override suspend fun setOfflineTranscriptionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_TRANSCRIPTION_ENABLED] = enabled
        }
    }

    override suspend fun setSelectedLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = language
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    override suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    override suspend fun setIsFirstLaunch(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = isFirst
        }
    }

    override suspend fun setLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = timestamp
        }
    }

    override suspend fun setCloudSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] = enabled
        }
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = currentCount + 1
        }
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = 0
        }
    }

    override suspend fun setIsPremium(isPremium: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] = isPremium
        }
    }

    override suspend fun setSubscriptionType(type: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIPTION_TYPE] = type
        }
    }

    override suspend fun setSubscriptionExpiryDate(date: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIPTION_EXPIRY_DATE] = date
        }
    }

    override suspend fun setDetectSpeakers(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DETECT_SPEAKERS] = enabled
        }
    }

    override suspend fun setTranscriptionLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] = language
        }
    }

    override suspend fun setAudioQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality
        }
    }

    override suspend fun setCloudProvider(provider: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_PROVIDER] = provider
        }
    }

    override suspend fun setButtonTriggerDelay(delay: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUTTON_TRIGGER_DELAY] = delay
        }
    }

    override suspend fun setOpenaiApiKey(key: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.OPENAI_API_KEY] = key
        }
    }

    override suspend fun setShowSilentNotifications(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SILENT_NOTIFICATIONS] = show
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
} 