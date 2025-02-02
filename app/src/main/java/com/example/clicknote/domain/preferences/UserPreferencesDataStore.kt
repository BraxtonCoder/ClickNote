package com.example.clicknote.domain.preferences

import com.example.clicknote.domain.model.CloudProvider
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataStore {
    val callRecordingEnabled: Flow<Boolean>
    val audioSavingEnabled: Flow<Boolean>
    val offlineTranscriptionEnabled: Flow<Boolean>
    val selectedLanguage: Flow<String>
    val notificationsEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val themeMode: Flow<String>
    val isFirstLaunch: Flow<Boolean>
    val lastSyncTime: Flow<Long>
    val cloudSyncEnabled: Flow<Boolean>
    val weeklyTranscriptionCount: Flow<Int>
    val detectSpeakers: Flow<Boolean>
    val transcriptionLanguage: Flow<String>
    val audioQuality: Flow<String>
    val cloudProvider: Flow<CloudProvider>
    val buttonTriggerDelay: Flow<Long>
    val openaiApiKey: Flow<String>
    val showSilentNotifications: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>
    val isFirstTimeUser: Flow<Boolean>
    val lastTranscriptionReset: Flow<Long>

    suspend fun setCallRecordingEnabled(enabled: Boolean)
    suspend fun setAudioSavingEnabled(enabled: Boolean)
    suspend fun setOfflineTranscriptionEnabled(enabled: Boolean)
    suspend fun setSelectedLanguage(language: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setIsFirstLaunch(isFirst: Boolean)
    suspend fun setLastSyncTime(time: Long)
    suspend fun setCloudSyncEnabled(enabled: Boolean)
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
    suspend fun setDetectSpeakers(enabled: Boolean)
    suspend fun setTranscriptionLanguage(language: String)
    suspend fun setAudioQuality(quality: String)
    suspend fun setCloudProvider(provider: CloudProvider)
    suspend fun setButtonTriggerDelay(delay: Long)
    suspend fun setOpenaiApiKey(key: String)
    suspend fun setShowSilentNotifications(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setIsFirstTimeUser(isFirst: Boolean)
    suspend fun setLastTranscriptionReset(time: Long)
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