package com.example.clicknote.domain.preferences

import com.example.clicknote.domain.model.CloudProvider
import com.example.clicknote.domain.model.TranscriptionLanguage
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataStore {
    val saveAudioFiles: Flow<Boolean>
    val highQualityAudio: Flow<Boolean>
    val backgroundRecording: Flow<Boolean>
    val offlineTranscription: Flow<Boolean>
    val autoPunctuation: Flow<Boolean>
    val showNotifications: Flow<Boolean>
    val notificationActions: Flow<Boolean>
    val vibrationFeedback: Flow<Boolean>
    val highContrast: Flow<Boolean>
    val transcriptionLanguage: Flow<TranscriptionLanguage>
    val cloudSyncEnabled: Flow<Boolean>
    val autoBackupEnabled: Flow<Boolean>
    val lastBackupTimestamp: Flow<Long>
    val transcriptionCount: Flow<Int>
    val lastTranscriptionReset: Flow<Long>

    suspend fun setSaveAudioFiles(enabled: Boolean)
    suspend fun setHighQualityAudio(enabled: Boolean)
    suspend fun setBackgroundRecording(enabled: Boolean)
    suspend fun setOfflineTranscription(enabled: Boolean)
    suspend fun setAutoPunctuation(enabled: Boolean)
    suspend fun setShowNotifications(enabled: Boolean)
    suspend fun setNotificationActions(enabled: Boolean)
    suspend fun setVibrationFeedback(enabled: Boolean)
    suspend fun setHighContrast(enabled: Boolean)
    suspend fun setTranscriptionLanguage(language: TranscriptionLanguage)
    suspend fun setCloudSyncEnabled(enabled: Boolean)
    suspend fun setAutoBackupEnabled(enabled: Boolean)
    suspend fun setLastBackupTimestamp(timestamp: Long)
    suspend fun incrementTranscriptionCount()
    suspend fun resetTranscriptionCount()
    suspend fun setLastTranscriptionReset(timestamp: Long)
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