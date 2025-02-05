package com.example.clicknote.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.clicknote.R
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.service.NotificationService
import com.example.clicknote.di.ServiceNotificationManager
import com.example.clicknote.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ServiceNotificationManager private val notificationManager: NotificationManager
) : NotificationService {

    companion object {
        private const val CHANNEL_ID = "clicknote_transcription_channel"
        private const val CHANNEL_NAME = "Transcription Notifications"
        private const val CHANNEL_DESCRIPTION = "Silent notifications for transcribed notes"
        private const val NOTIFICATION_GROUP = "clicknote_notifications"
        private const val RECORDING_NOTIFICATION_ID = 1001
        private const val SYNC_NOTIFICATION_ID = 1002
        private const val ERROR_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannel()
    }

    override fun showTranscriptionNotification(note: Note) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("note_id", note.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            note.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Note Transcribed")
            .setContentText(note.content.take(100) + if (note.content.length > 100) "..." else "")
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_copy,
                "Copy",
                createCopyAction(note)
            )
            .addAction(
                R.drawable.ic_share,
                "Share",
                createShareAction(note)
            )
            .build()

        notificationManager.notify(note.id.hashCode(), notification)
    }

    override fun showRecordingNotification(isRecording: Boolean) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(if (isRecording) "Recording..." else "Recording Stopped")
            .setOngoing(isRecording)
            .setSilent(true)
            .build()

        notificationManager.notify(RECORDING_NOTIFICATION_ID, notification)
    }

    override fun showSyncNotification(isSyncing: Boolean) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle(if (isSyncing) "Syncing notes..." else "Sync complete")
            .setOngoing(isSyncing)
            .setSilent(true)
            .build()

        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }

    override fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Error")
            .setContentText(message)
            .setAutoCancel(true)
            .setSilent(true)
            .build()

        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    override fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(true)
                enableVibration(false)
                enableLights(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createCopyAction(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.clicknote.COPY_NOTE"
            putExtra("note_id", note.id)
        }
        return PendingIntent.getActivity(
            context,
            note.id.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createShareAction(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.clicknote.SHARE_NOTE"
            putExtra("note_id", note.id)
        }
        return PendingIntent.getActivity(
            context,
            note.id.hashCode() + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
} 