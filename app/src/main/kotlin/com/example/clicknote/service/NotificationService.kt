package com.example.clicknote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.clicknote.R
import com.example.clicknote.ui.MainActivity
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TranscriptionState
import dagger.hilt.android.qualifiers.ApplicationContext

abstract class NotificationService(
    @ApplicationContext protected val context: Context
) {
    protected val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    abstract fun createTranscriptionNotification(state: TranscriptionState): Notification
    abstract fun showTranscriptionNotification(state: TranscriptionState)
    abstract fun updateTranscriptionNotification(state: TranscriptionState)
    abstract fun cancelTranscriptionNotification()
    abstract fun createSilentNotification(text: String): Notification
    abstract fun showSilentNotification(text: String, id: Int)
    abstract fun cancelSilentNotification(id: Int)
    abstract fun cancelAllNotifications()
    abstract fun createForegroundNotification(service: android.app.Service)
    abstract fun updateRecordingProgress(amplitude: Float)
    abstract fun stopForegroundNotification(service: android.app.Service)
    abstract fun showTranscriptionNotification(note: Note)
    abstract fun showRecordingNotification()
    abstract fun showTranscribingNotification()
    abstract fun hideRecordingNotification()
    abstract fun hideTranscribingNotification()
    abstract fun showPremiumLimitNotification()
    abstract fun cancelNotification(id: Int)

    protected fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        description: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description?.let { this.description = it }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_USAGE = "usage_alerts"
        const val NOTIFICATION_ID_USAGE_WARNING = 1001
        const val NOTIFICATION_ID_USAGE_LIMIT = 1002
    }
} 