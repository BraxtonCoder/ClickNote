package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionLanguage
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    // Transcription Settings
    val isOnlineTranscriptionEnabled: Flow<Boolean>
    val isOfflineTranscriptionEnabled: Flow<Boolean>
    val transcriptionLanguage: Flow<TranscriptionLanguage>
    val detectSpeakers: Flow<Boolean>
    val audioQuality: Flow<String>

    // Audio Settings
    val isAudioSavingEnabled: Flow<Boolean>
    val isCallRecordingEnabled: Flow<Boolean>

    // UI Settings
    val themeMode: Flow<String>
    val showSilentNotifications: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>

    // Cloud Settings
    val isCloudSyncEnabled: Flow<Boolean>
    val lastSyncTime: Flow<Long>
    val openaiApiKey: Flow<String?>

    // App State
    val isFirstLaunch: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>
    val weeklyTranscriptionCount: Flow<Int>

    // Setters
    suspend fun setOnlineTranscriptionEnabled(enabled: Boolean)
    suspend fun setOfflineTranscriptionEnabled(enabled: Boolean)
    suspend fun setTranscriptionLanguage(language: TranscriptionLanguage)
    suspend fun setDetectSpeakers(enabled: Boolean)
    suspend fun setAudioQuality(quality: String)
    suspend fun setAudioSavingEnabled(enabled: Boolean)
    suspend fun setCallRecordingEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setShowSilentNotifications(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setCloudSyncEnabled(enabled: Boolean)
    suspend fun setLastSyncTime(timestamp: Long)
    suspend fun setOpenaiApiKey(key: String?)
    suspend fun setFirstLaunch(isFirst: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
} 