package com.example.clicknote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.clicknote.MainActivity
import com.example.clicknote.R
import com.example.clicknote.service.recording.AudioRecordingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "recording_channel"
        
        const val ACTION_RESUME = "com.example.clicknote.ACTION_RESUME"
        const val ACTION_PAUSE = "com.example.clicknote.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.clicknote.ACTION_STOP"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.recording_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.recording_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createInitialNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(context.getString(R.string.recording_notification_title))
            .setContentText(context.getString(R.string.recording_notification_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun showRecordingNotification(isPaused: Boolean) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = Intent(context, AudioRecordingService::class.java).apply {
            action = ACTION_RESUME
        }
        val resumePendingIntent = PendingIntent.getService(
            context,
            1,
            resumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AudioRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.recording_notification_title))
            .setContentText(context.getString(R.string.recording_notification_text))
            .setSmallIcon(if (isPaused) R.drawable.ic_mic_off else R.drawable.ic_mic)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_play,
                context.getString(R.string.resume),
                resumePendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
                context.getString(R.string.stop),
                stopPendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showTranscribingNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.transcribing_notification_title))
            .setContentText(context.getString(R.string.transcribing_notification_text))
            .setSmallIcon(R.drawable.ic_transcribe)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
} 