package com.example.clicknote.service.impl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.clicknote.R
import com.example.clicknote.domain.interfaces.NotificationHandler
import com.example.clicknote.domain.model.TranscriptionState
import com.example.clicknote.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationHandler {

    companion object {
        private const val CHANNEL_ID_TRANSCRIPTION = "transcription_channel"
        private const val CHANNEL_ID_SILENT = "silent_channel"
        private const val NOTIFICATION_ID_TRANSCRIPTION = 1
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transcriptionChannel = NotificationChannel(
                CHANNEL_ID_TRANSCRIPTION,
                "Transcription Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows the status of ongoing transcriptions"
                setShowBadge(true)
            }

            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Silent Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows transcribed notes without sound"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(transcriptionChannel, silentChannel))
        }
    }

    override fun createTranscriptionNotification(state: TranscriptionState): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_TRANSCRIPTION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        when (state) {
            is TranscriptionState.Recording -> {
                builder.setContentTitle("Recording...")
                    .setContentText("Tap to open")
                    .setProgress(0, 0, true)
            }
            is TranscriptionState.Processing -> {
                builder.setContentTitle("Processing transcription...")
                    .setContentText("Progress: ${(state.progress * 100).toInt()}%")
                    .setProgress(100, (state.progress * 100).toInt(), false)
            }
            is TranscriptionState.Completed -> {
                builder.setContentTitle("Transcription completed")
                    .setContentText("Duration: ${state.duration / 1000}s")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
            }
            is TranscriptionState.Error -> {
                builder.setContentTitle("Transcription failed")
                    .setContentText(state.error.message ?: "Unknown error")
                    .setProgress(0, 0, false)
                    .setAutoCancel(true)
                    .setOngoing(false)
            }
            else -> {
                builder.setContentTitle("ClickNote")
                    .setContentText("Ready to record")
            }
        }

        return builder.build()
    }

    override fun showTranscriptionNotification(state: TranscriptionState) {
        val notification = createTranscriptionNotification(state)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TRANSCRIPTION, notification)
    }

    override fun updateTranscriptionNotification(state: TranscriptionState) {
        showTranscriptionNotification(state)
    }

    override fun cancelTranscriptionNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_TRANSCRIPTION)
    }

    override fun createSilentNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_SILENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New note transcribed")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_copy,
                "Copy",
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent("com.example.clicknote.COPY_NOTE").putExtra("text", text),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                R.drawable.ic_share,
                "Share",
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent("com.example.clicknote.SHARE_NOTE").putExtra("text", text),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    override fun showSilentNotification(text: String, id: Int) {
        val notification = createSilentNotification(text)
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    override fun cancelSilentNotification(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    override fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
} 