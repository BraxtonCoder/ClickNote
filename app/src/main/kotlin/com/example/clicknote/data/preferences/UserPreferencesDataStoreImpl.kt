package com.example.clicknote.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.clicknote.domain.model.CloudProvider
import com.example.clicknote.domain.model.CloudStorageType
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {

    private object PreferencesKeys {
        val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("transcription_language")
        val CLOUD_STORAGE_TYPE = stringPreferencesKey("cloud_storage_type")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val WEEKLY_TRANSCRIPTION_COUNT = intPreferencesKey("weekly_transcription_count")
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
        val DETECT_SPEAKERS = booleanPreferencesKey("detect_speakers")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val CLOUD_PROVIDER = stringPreferencesKey("cloud_provider")
        val BUTTON_TRIGGER_DELAY = longPreferencesKey("button_trigger_delay")
        val SHOW_SILENT_NOTIFICATIONS = booleanPreferencesKey("show_silent_notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_FIRST_TIME_USER = booleanPreferencesKey("is_first_time_user")
        val LAST_TRANSCRIPTION_RESET = longPreferencesKey("last_transcription_reset")
        val ONLINE_TRANSCRIPTION_ENABLED = booleanPreferencesKey("online_transcription_enabled")
        val SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")
        val OFFLINE_MODE_ENABLED = booleanPreferencesKey("offline_mode_enabled")
        val LAST_TRANSCRIPTION_RESET_TIME = longPreferencesKey("last_transcription_reset_time")
    }

    private val onlineTranscriptionEnabledKey = booleanPreferencesKey("online_transcription_enabled")

    override val callRecordingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CALL_RECORDING_ENABLED] ?: false }

    override val audioSavingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.AUDIO_SAVING_ENABLED] ?: false }

    override val offlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.OFFLINE_TRANSCRIPTION_ENABLED] ?: false }

    override val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "" }

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: false }

    override val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.VIBRATION_ENABLED] ?: false }

    override val themeMode: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.THEME_MODE] ?: "system" }

    override val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true }

    override val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_SYNC_TIME] ?: 0L }

    override val cloudSyncEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] ?: false }

    override val detectSpeakers: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DETECT_SPEAKERS] ?: false }

    override val transcriptionLanguage: Flow<TranscriptionLanguage> = context.dataStore.data
        .map { preferences ->
            val languageCode = preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] ?: TranscriptionLanguage.AUTO_DETECT.code
            TranscriptionLanguage.fromCode(languageCode)
        }

    override val audioQuality: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.AUDIO_QUALITY] ?: "high" }

    override val cloudProvider: Flow<CloudProvider> = context.dataStore.data
        .map { preferences ->
            val provider = preferences[PreferencesKeys.CLOUD_PROVIDER] ?: CloudProvider.LOCAL.name
            try {
                CloudProvider.valueOf(provider)
            } catch (e: IllegalArgumentException) {
                CloudProvider.LOCAL
            }
        }

    override val buttonTriggerDelay: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.BUTTON_TRIGGER_DELAY] ?: 750L }

    override val openaiApiKey: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.OPENAI_API_KEY] }

    override val showSilentNotifications: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SHOW_SILENT_NOTIFICATIONS] ?: true }

    override val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }

    override val isFirstTimeUser: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.IS_FIRST_TIME_USER] ?: true }

    override val lastTranscriptionReset: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_TRANSCRIPTION_RESET] ?: 0L }

    override val cloudStorageType: Flow<CloudStorageType> = context.dataStore.data
        .map { preferences ->
            val typeString = preferences[PreferencesKeys.CLOUD_STORAGE_TYPE] ?: CloudStorageType.NONE.name
            try {
                CloudStorageType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
                CloudStorageType.NONE
            }
        }

    override val weeklyTranscriptionCount: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0 }

    override val onlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[onlineTranscriptionEnabledKey] ?: true // Default to true
        }

    override val subscriptionStatus: Flow<SubscriptionStatus> = context.dataStore.data
        .map { preferences ->
            val status = preferences[PreferencesKeys.SUBSCRIPTION_STATUS] ?: SubscriptionStatus.Free.toString()
            SubscriptionStatus.fromString(status)
        }

    override val offlineModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE_ENABLED] ?: false
        }

    override val isOnlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[onlineTranscriptionEnabledKey] ?: true // Default to true
        }

    override val lastTranscriptionResetTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_TRANSCRIPTION_RESET_TIME] ?: System.currentTimeMillis()
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

    override suspend fun setLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = timestamp
        }
    }

    override suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] = enabled
        }
    }

    override suspend fun setDetectSpeakers(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DETECT_SPEAKERS] = enabled
        }
    }

    override suspend fun setTranscriptionLanguage(language: TranscriptionLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] = language.code
        }
    }

    override suspend fun setAudioQuality(quality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_QUALITY] = quality
        }
    }

    override suspend fun setCloudProvider(provider: CloudProvider) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_PROVIDER] = provider.name
        }
    }

    override suspend fun setButtonTriggerDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUTTON_TRIGGER_DELAY] = delayMs
        }
    }

    override suspend fun setOpenaiApiKey(key: String?) {
        context.dataStore.edit { preferences ->
            if (key != null) {
                preferences[PreferencesKeys.OPENAI_API_KEY] = key
            } else {
                preferences.remove(PreferencesKeys.OPENAI_API_KEY)
            }
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

    override suspend fun setLastTranscriptionReset(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TRANSCRIPTION_RESET] = timestamp
        }
    }

    override suspend fun setCloudStorageType(type: CloudStorageType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_STORAGE_TYPE] = type.name
        }
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        checkAndResetIfNeeded()
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = currentCount + 1
        }
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = 0
            preferences[PreferencesKeys.LAST_TRANSCRIPTION_RESET_TIME] = System.currentTimeMillis()
        }
    }

    override suspend fun setOnlineTranscriptionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[onlineTranscriptionEnabledKey] = enabled
        }
    }

    override suspend fun setSubscriptionStatus(status: SubscriptionStatus) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIPTION_STATUS] = SubscriptionStatus.toString(status)
        }
    }

    override suspend fun setOfflineModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE_ENABLED] = enabled
        }
    }

    override suspend fun getOnlineTranscriptionEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[onlineTranscriptionEnabledKey] ?: true
        }.first()
    }

    override suspend fun getRemainingFreeTranscriptions(): Int {
        checkAndResetIfNeeded()
        val currentCount = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
        }.first()
        return (3 - currentCount).coerceAtLeast(0)
    }

    private suspend fun checkAndResetIfNeeded() {
        val lastResetTime = lastTranscriptionResetTime.first()
        val currentTime = System.currentTimeMillis()
        val weekInMillis = TimeUnit.DAYS.toMillis(7)
        
        if (currentTime - lastResetTime >= weekInMillis) {
            resetWeeklyTranscriptionCount()
        }
    }
}