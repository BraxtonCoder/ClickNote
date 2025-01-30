package com.example.clicknote.domain.preferences

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
    val isPremium: Flow<Boolean>
    val subscriptionType: Flow<String>
    val subscriptionExpiryDate: Flow<Long>
    val detectSpeakers: Flow<Boolean>
    val transcriptionLanguage: Flow<String>
    val audioQuality: Flow<String>
    val cloudProvider: Flow<String>
    val buttonTriggerDelay: Flow<Long>
    val openaiApiKey: Flow<String>
    val showSilentNotifications: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>

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
    suspend fun setIsPremium(isPremium: Boolean)
    suspend fun setSubscriptionType(type: String)
    suspend fun setSubscriptionExpiryDate(date: Long)
    suspend fun setDetectSpeakers(enabled: Boolean)
    suspend fun setTranscriptionLanguage(language: String)
    suspend fun setAudioQuality(quality: String)
    suspend fun setCloudProvider(provider: String)
    suspend fun setButtonTriggerDelay(delay: Long)
    suspend fun setOpenaiApiKey(key: String)
    suspend fun setShowSilentNotifications(show: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
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