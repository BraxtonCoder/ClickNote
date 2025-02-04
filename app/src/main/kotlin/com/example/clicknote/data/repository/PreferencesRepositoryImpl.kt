package com.example.clicknote.data.repository

import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferences: UserPreferencesDataStore
) : PreferencesRepository {

    override val isOnlineTranscriptionEnabled: Flow<Boolean> = preferences.onlineTranscriptionEnabled
    override val isOfflineTranscriptionEnabled: Flow<Boolean> = preferences.offlineTranscriptionEnabled
    override val transcriptionLanguage: Flow<TranscriptionLanguage> = preferences.transcriptionLanguage
    override val detectSpeakers: Flow<Boolean> = preferences.detectSpeakers
    override val audioQuality: Flow<String> = preferences.audioQuality

    override val isAudioSavingEnabled: Flow<Boolean> = preferences.audioSavingEnabled
    override val isCallRecordingEnabled: Flow<Boolean> = preferences.callRecordingEnabled

    override val themeMode: Flow<String> = preferences.themeMode
    override val showSilentNotifications: Flow<Boolean> = preferences.showSilentNotifications
    override val vibrationEnabled: Flow<Boolean> = preferences.vibrationEnabled

    override val isCloudSyncEnabled: Flow<Boolean> = preferences.cloudSyncEnabled
    override val lastSyncTime: Flow<Long> = preferences.lastSyncTime
    override val openaiApiKey: Flow<String?> = preferences.openaiApiKey

    override val isFirstLaunch: Flow<Boolean> = preferences.isFirstLaunch
    override val onboardingCompleted: Flow<Boolean> = preferences.onboardingCompleted
    override val weeklyTranscriptionCount: Flow<Int> = preferences.weeklyTranscriptionCount

    override suspend fun setOnlineTranscriptionEnabled(enabled: Boolean) {
        preferences.setOnlineTranscriptionEnabled(enabled)
    }

    override suspend fun setOfflineTranscriptionEnabled(enabled: Boolean) {
        preferences.setOfflineTranscriptionEnabled(enabled)
    }

    override suspend fun setTranscriptionLanguage(language: TranscriptionLanguage) {
        preferences.setTranscriptionLanguage(language)
    }

    override suspend fun setDetectSpeakers(enabled: Boolean) {
        preferences.setDetectSpeakers(enabled)
    }

    override suspend fun setAudioQuality(quality: String) {
        preferences.setAudioQuality(quality)
    }

    override suspend fun setAudioSavingEnabled(enabled: Boolean) {
        preferences.setAudioSavingEnabled(enabled)
    }

    override suspend fun setCallRecordingEnabled(enabled: Boolean) {
        preferences.setCallRecordingEnabled(enabled)
    }

    override suspend fun setThemeMode(mode: String) {
        preferences.setThemeMode(mode)
    }

    override suspend fun setShowSilentNotifications(enabled: Boolean) {
        preferences.setShowSilentNotifications(enabled)
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        preferences.setVibrationEnabled(enabled)
    }

    override suspend fun setCloudSyncEnabled(enabled: Boolean) {
        preferences.setCloudSyncEnabled(enabled)
    }

    override suspend fun setLastSyncTime(timestamp: Long) {
        preferences.setLastSyncTime(timestamp)
    }

    override suspend fun setOpenaiApiKey(key: String?) {
        preferences.setOpenaiApiKey(key)
    }

    override suspend fun setFirstLaunch(isFirst: Boolean) {
        preferences.setIsFirstLaunch(isFirst)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferences.setOnboardingCompleted(completed)
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        preferences.incrementWeeklyTranscriptionCount()
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        preferences.resetWeeklyTranscriptionCount()
    }
}