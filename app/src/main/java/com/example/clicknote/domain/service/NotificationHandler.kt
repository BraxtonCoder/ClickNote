package com.example.clicknote.domain.service

interface NotificationHandler {
    fun showTranscriptionNotification(noteId: String, preview: String)
    fun showRecordingNotification()
    fun showSyncNotification(progress: Float)
    fun showBackupNotification(progress: Float)
    fun showErrorNotification(message: String)
    fun showSuccessNotification(message: String)
    fun cancelNotification(id: Int)
    fun cancelAllNotifications()
    fun updateRecordingProgress(duration: Long)
    fun updateTranscriptionProgress(progress: Float)
}

object NotificationIds {
    const val RECORDING = 1
    const val TRANSCRIPTION = 2
    const val SYNC = 3
    const val BACKUP = 4
    const val ERROR = 5
    const val SUCCESS = 6
} 