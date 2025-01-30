package com.example.clicknote.service.notification

import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat

interface NotificationService {
    fun createRecordingNotification(
        title: String,
        content: String,
        isRecording: Boolean,
        actions: List<NotificationAction> = emptyList()
    ): Notification

    fun createTranscriptionNotification(
        title: String,
        content: String,
        progress: Int? = null,
        actions: List<NotificationAction> = emptyList()
    ): Notification

    fun showNotification(id: Int, notification: Notification)
    fun updateNotification(id: Int, notification: Notification)
    fun cancelNotification(id: Int)
    fun createNotificationChannel(channelId: String, channelName: String, importance: Int)
}

data class NotificationAction(
    val icon: Int,
    val title: String,
    val intent: PendingIntent,
    val showOnLockScreen: Boolean = false
)

object NotificationConstants {
    const val CHANNEL_ID_RECORDING = "recording_channel"
    const val CHANNEL_ID_TRANSCRIPTION = "transcription_channel"
    const val CHANNEL_ID_BACKUP = "backup_channel"
    
    const val NOTIFICATION_ID_RECORDING = 1
    const val NOTIFICATION_ID_TRANSCRIPTION = 2
    const val NOTIFICATION_ID_BACKUP = 3
    
    const val ACTION_STOP_RECORDING = "stop_recording"
    const val ACTION_PAUSE_RECORDING = "pause_recording"
    const val ACTION_RESUME_RECORDING = "resume_recording"
    const val ACTION_CANCEL_TRANSCRIPTION = "cancel_transcription"
} 