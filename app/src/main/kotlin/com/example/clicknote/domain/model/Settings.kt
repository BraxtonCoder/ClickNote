package com.example.clicknote.domain.model

data class Settings(
    val saveAudioFiles: Boolean = true,
    val highQualityAudio: Boolean = false,
    val backgroundRecording: Boolean = false,
    val storageLocation: StorageLocation = StorageLocation.LOCAL,
    val autoBackup: Boolean = false,
    val transcriptionLanguage: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val offlineTranscription: Boolean = true,
    val autoPunctuation: Boolean = true,
    val showNotifications: Boolean = true,
    val notificationActions: Boolean = true,
    val vibrationFeedback: Boolean = true,
    val highContrast: Boolean = false
) 