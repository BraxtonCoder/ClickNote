package com.example.clicknote.domain.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.clicknote.domain.model.CloudProvider
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.model.CloudStorageType
import com.example.clicknote.domain.model.SubscriptionStatus

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface UserPreferencesDataStore {
    val callRecordingEnabled: Flow<Boolean>
    val audioSavingEnabled: Flow<Boolean>
    val offlineTranscriptionEnabled: Flow<Boolean>
    val onlineTranscriptionEnabled: Flow<Boolean>
    val selectedLanguage: Flow<String>
    val notificationsEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val themeMode: Flow<String>
    val isFirstLaunch: Flow<Boolean>
    val lastSyncTime: Flow<Long>
    val cloudSyncEnabled: Flow<Boolean>
    val weeklyTranscriptionCount: Flow<Int>
    val detectSpeakers: Flow<Boolean>
    val transcriptionLanguage: Flow<TranscriptionLanguage>
    val audioQuality: Flow<String>
    val cloudProvider: Flow<CloudProvider>
    val buttonTriggerDelay: Flow<Long>
    val openaiApiKey: Flow<String?>
    val showSilentNotifications: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>
    val isFirstTimeUser: Flow<Boolean>
    val lastTranscriptionReset: Flow<Long>
    val cloudStorageType: Flow<CloudStorageType>
    val subscriptionStatus: Flow<SubscriptionStatus>
    val offlineModeEnabled: Flow<Boolean>
    val isOnlineTranscriptionEnabled: Flow<Boolean>
    val lastTranscriptionResetTime: Flow<Long>

    suspend fun setCallRecordingEnabled(enabled: Boolean)
    suspend fun setAudioSavingEnabled(enabled: Boolean)
    suspend fun setOfflineTranscriptionEnabled(enabled: Boolean)
    suspend fun setOnlineTranscriptionEnabled(enabled: Boolean)
    suspend fun setSelectedLanguage(language: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setIsFirstLaunch(isFirst: Boolean)
    suspend fun setLastSyncTime(timestamp: Long)
    suspend fun setCloudSyncEnabled(enabled: Boolean)
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
    suspend fun setDetectSpeakers(enabled: Boolean)
    suspend fun setTranscriptionLanguage(language: TranscriptionLanguage)
    suspend fun setAudioQuality(quality: String)
    suspend fun setCloudProvider(provider: CloudProvider)
    suspend fun setButtonTriggerDelay(delayMs: Long)
    suspend fun setOpenaiApiKey(key: String?)
    suspend fun setShowSilentNotifications(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setIsFirstTimeUser(isFirst: Boolean)
    suspend fun setLastTranscriptionReset(timestamp: Long)
    suspend fun setCloudStorageType(type: CloudStorageType)
    suspend fun setSubscriptionStatus(status: SubscriptionStatus)
    suspend fun setOfflineModeEnabled(enabled: Boolean)
    suspend fun getOnlineTranscriptionEnabled(): Boolean
    suspend fun getRemainingFreeTranscriptions(): Int
}

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {
    private val firstLaunchKey = booleanPreferencesKey("first_launch")

    override val callRecordingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("call_recording_enabled")] ?: false
        }

    override val audioSavingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("audio_saving_enabled")] ?: false
        }

    override val offlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("offline_transcription_enabled")] ?: false
        }

    override val onlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("online_transcription_enabled")] ?: false
        }

    override val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("selected_language")] ?: ""
        }

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("notifications_enabled")] ?: false
        }

    override val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("vibration_enabled")] ?: false
        }

    override val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("theme_mode")] ?: ""
        }

    override val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[firstLaunchKey] ?: true
        }

    override val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("last_sync_time")] ?: 0L
        }

    override val cloudSyncEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("cloud_sync_enabled")] ?: false
        }

    override val weeklyTranscriptionCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("weekly_transcription_count")] ?: 0
        }

    override val detectSpeakers: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("detect_speakers")] ?: false
        }

    override val transcriptionLanguage: Flow<TranscriptionLanguage> = context.dataStore.data
        .map { preferences ->
            TranscriptionLanguage.values()[preferences[booleanPreferencesKey("transcription_language")]?.toInt() ?: 0]
        }

    override val audioQuality: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("audio_quality")] ?: ""
        }

    override val cloudProvider: Flow<CloudProvider> = context.dataStore.data
        .map { preferences ->
            CloudProvider.values()[preferences[booleanPreferencesKey("cloud_provider")]?.toInt() ?: 0]
        }

    override val buttonTriggerDelay: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("button_trigger_delay")] ?: 0L
        }

    override val openaiApiKey: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("openai_api_key")]
        }

    override val showSilentNotifications: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("show_silent_notifications")] ?: false
        }

    override val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("onboarding_completed")] ?: false
        }

    override val isFirstTimeUser: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("is_first_time_user")] ?: true
        }

    override val lastTranscriptionReset: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("last_transcription_reset")] ?: 0L
        }

    override val cloudStorageType: Flow<CloudStorageType> = context.dataStore.data
        .map { preferences ->
            CloudStorageType.values()[preferences[booleanPreferencesKey("cloud_storage_type")]?.toInt() ?: 0]
        }

    override val subscriptionStatus: Flow<SubscriptionStatus> = context.dataStore.data
        .map { preferences ->
            SubscriptionStatus.values()[preferences[booleanPreferencesKey("subscription_status")]?.toInt() ?: 0]
        }

    override val offlineModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("offline_mode_enabled")] ?: false
        }

    override val isOnlineTranscriptionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("online_transcription_enabled")] ?: false
        }

    override val lastTranscriptionResetTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey("last_transcription_reset_time")] ?: 0L
        }

    override suspend fun setCallRecordingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("call_recording_enabled")] = enabled
        }
    }

    override suspend fun setAudioSavingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("audio_saving_enabled")] = enabled
        }
    }

    override suspend fun setOfflineTranscriptionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("offline_transcription_enabled")] = enabled
        }
    }

    override suspend fun setOnlineTranscriptionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("online_transcription_enabled")] = enabled
        }
    }

    override suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("selected_language")] = language
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("notifications_enabled")] = enabled
        }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("vibration_enabled")] = enabled
        }
    }

    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("theme_mode")] = mode
        }
    }

    override suspend fun setIsFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[firstLaunchKey] = isFirst
        }
    }

    override suspend fun setLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("last_sync_time")] = timestamp
        }
    }

    override suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("cloud_sync_enabled")] = enabled
        }
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            val count = preferences[booleanPreferencesKey("weekly_transcription_count")] ?: 0
            preferences[booleanPreferencesKey("weekly_transcription_count")] = count + 1
        }
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("last_backup_timestamp")] = timestamp
        }
    }

    suspend fun incrementTranscriptionCount() {
        context.dataStore.edit { preferences ->
            val count = preferences[booleanPreferencesKey("transcription_count")] ?: 0
            preferences[booleanPreferencesKey("transcription_count")] = count + 1
        }
    }

    suspend fun resetTranscriptionCount() {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("transcription_count")] = 0
        }
    }

    suspend fun setLastTranscriptionReset(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("last_transcription_reset")] = timestamp
        }
    }

    override suspend fun setCloudStorageType(type: CloudStorageType) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("cloud_storage_type")] = type.ordinal
        }
    }

    override suspend fun setSubscriptionStatus(status: SubscriptionStatus) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("subscription_status")] = status.ordinal
        }
    }

    override suspend fun setOfflineModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("offline_mode_enabled")] = enabled
        }
    }

    override suspend fun getOnlineTranscriptionEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("online_transcription_enabled")] ?: false
        }.first()
    }

    override suspend fun getRemainingFreeTranscriptions(): Int {
        // Implementation of getRemainingFreeTranscriptions method
        // This is a placeholder and should be implemented based on your specific requirements
        return 0 // Placeholder return, actual implementation needed
    }
}

enum class AudioQuality {
    HIGH,
    MEDIUM,
    LOW
}

enum class CloudProvider {
    AWS,
    AZURE,
    GOOGLE
} 