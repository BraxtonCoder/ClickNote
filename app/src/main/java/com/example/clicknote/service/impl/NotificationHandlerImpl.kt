package com.example.clicknote.service.impl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.clicknote.R
import com.example.clicknote.domain.model.Note
import com.example.clicknote.service.NotificationHandler
import com.example.clicknote.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
    private val notificationManagerCompat: NotificationManagerCompat
) : NotificationHandler {

    companion object {
        private const val CHANNEL_ID_RECORDING = "recording_channel"
        private const val CHANNEL_ID_TRANSCRIPTION = "transcription_channel"
        private const val CHANNEL_ID_PREMIUM = "premium_channel"
        
        private const val NOTIFICATION_ID_RECORDING = 1
        private const val NOTIFICATION_ID_TRANSCRIPTION = 2
        private const val NOTIFICATION_ID_PREMIUM = 3
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val recordingChannel = NotificationChannel(
                CHANNEL_ID_RECORDING,
                context.getString(R.string.channel_recording_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_recording_description)
            }

            val transcriptionChannel = NotificationChannel(
                CHANNEL_ID_TRANSCRIPTION,
                context.getString(R.string.channel_transcription_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_transcription_description)
            }

            val premiumChannel = NotificationChannel(
                CHANNEL_ID_PREMIUM,
                context.getString(R.string.channel_premium_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_premium_description)
            }

            notificationManager.createNotificationChannels(
                listOf(recordingChannel, transcriptionChannel, premiumChannel)
            )
        }
    }

    override fun showRecordingNotification(isRecording: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECORDING)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(context.getString(if (isRecording) R.string.recording_active else R.string.recording_paused))
            .setContentText(context.getString(R.string.tap_to_open))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManagerCompat.notify(NOTIFICATION_ID_RECORDING, notification)
    }

    override fun showTranscriptionNotification(note: Note) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", note.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSCRIPTION)
            .setSmallIcon(R.drawable.ic_note)
            .setContentTitle(context.getString(R.string.transcription_complete))
            .setContentText(note.content.take(100) + if (note.content.length > 100) "..." else "")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_copy,
                context.getString(R.string.copy),
                createCopyPendingIntent(note)
            )
            .addAction(
                R.drawable.ic_share,
                context.getString(R.string.share),
                createSharePendingIntent(note)
            )
            .build()

        notificationManagerCompat.notify(NOTIFICATION_ID_TRANSCRIPTION, notification)
    }

    override fun showPremiumNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_premium", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PREMIUM)
            .setSmallIcon(R.drawable.ic_premium)
            .setContentTitle(context.getString(R.string.premium_required))
            .setContentText(context.getString(R.string.premium_notification_message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManagerCompat.notify(NOTIFICATION_ID_PREMIUM, notification)
    }

    override fun cancelRecordingNotification() {
        notificationManagerCompat.cancel(NOTIFICATION_ID_RECORDING)
    }

    override fun cancelTranscriptionNotification() {
        notificationManagerCompat.cancel(NOTIFICATION_ID_TRANSCRIPTION)
    }

    override fun cancelAllNotifications() {
        notificationManagerCompat.cancelAll()
    }

    override fun cleanup() {
        cancelAllNotifications()
    }

    private fun createCopyPendingIntent(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "COPY_NOTE"
            putExtra("note_id", note.id)
        }
        return PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSharePendingIntent(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "SHARE_NOTE"
            putExtra("note_id", note.id)
        }
        return PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
} 