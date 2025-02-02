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
import com.example.clicknote.domain.service.NotificationHandler
import com.example.clicknote.domain.service.NotificationIds
import com.example.clicknote.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) : NotificationHandler {

    companion object {
        private const val CHANNEL_ID_RECORDING = "recording_channel"
        private const val CHANNEL_ID_TRANSCRIPTION = "transcription_channel"
        private const val CHANNEL_ID_SYNC = "sync_channel"
        private const val CHANNEL_ID_BACKUP = "backup_channel"
        private const val CHANNEL_ID_ERROR = "error_channel"
        private const val CHANNEL_ID_SUCCESS = "success_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_RECORDING,
                    "Recording",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    CHANNEL_ID_TRANSCRIPTION,
                    "Transcription",
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    CHANNEL_ID_SYNC,
                    "Sync",
                    NotificationManager.IMPORTANCE_LOW
                ),
                NotificationChannel(
                    CHANNEL_ID_BACKUP,
                    "Backup",
                    NotificationManager.IMPORTANCE_LOW
                ),
                NotificationChannel(
                    CHANNEL_ID_ERROR,
                    "Errors",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    CHANNEL_ID_SUCCESS,
                    "Success",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun showRecordingNotification() {
        val notification = createBaseNotification(CHANNEL_ID_RECORDING)
            .setContentTitle("Recording in progress")
            .setContentText("Tap to view")
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .addAction(createStopAction())
            .addAction(createPauseAction())
            .build()

        notificationManager.notify(NotificationIds.RECORDING, notification)
    }

    override fun showTranscriptionNotification(noteId: String, preview: String) {
        val notification = createBaseNotification(CHANNEL_ID_TRANSCRIPTION)
            .setContentTitle("New note created")
            .setContentText(preview)
            .setSmallIcon(R.drawable.ic_note)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationIds.TRANSCRIPTION, notification)
    }

    override fun showSyncNotification(progress: Float) {
        val notification = createBaseNotification(CHANNEL_ID_SYNC)
            .setContentTitle("Syncing notes")
            .setProgress(100, (progress * 100).toInt(), false)
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(true)
            .build()

        notificationManager.notify(NotificationIds.SYNC, notification)
    }

    override fun showBackupNotification(progress: Float) {
        val notification = createBaseNotification(CHANNEL_ID_BACKUP)
            .setContentTitle("Backing up notes")
            .setProgress(100, (progress * 100).toInt(), false)
            .setSmallIcon(R.drawable.ic_backup)
            .setOngoing(true)
            .build()

        notificationManager.notify(NotificationIds.BACKUP, notification)
    }

    override fun showErrorNotification(message: String) {
        val notification = createBaseNotification(CHANNEL_ID_ERROR)
            .setContentTitle("Error")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_error)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationIds.ERROR, notification)
    }

    override fun showSuccessNotification(message: String) {
        val notification = createBaseNotification(CHANNEL_ID_SUCCESS)
            .setContentTitle("Success")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_success)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationIds.SUCCESS, notification)
    }

    override fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    override fun updateRecordingProgress(duration: Long) {
        val notification = createBaseNotification(CHANNEL_ID_RECORDING)
            .setContentTitle("Recording in progress")
            .setContentText(formatDuration(duration))
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .addAction(createStopAction())
            .addAction(createPauseAction())
            .build()

        notificationManager.notify(NotificationIds.RECORDING, notification)
    }

    override fun updateTranscriptionProgress(progress: Float) {
        val notification = createBaseNotification(CHANNEL_ID_TRANSCRIPTION)
            .setContentTitle("Transcribing audio")
            .setProgress(100, (progress * 100).toInt(), false)
            .setSmallIcon(R.drawable.ic_transcribe)
            .setOngoing(true)
            .build()

        notificationManager.notify(NotificationIds.TRANSCRIPTION, notification)
    }

    override fun createRecordingNotification(): Notification {
        return createBaseNotification(CHANNEL_ID_RECORDING)
            .setContentTitle("Recording in progress")
            .setContentText("Tap to view")
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .addAction(createStopAction())
            .addAction(createPauseAction())
            .build()
    }

    private fun createBaseNotification(channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent())
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioRecordingForegroundService::class.java).apply {
            action = AudioRecordingForegroundService.ACTION_STOP_RECORDING
        }
        val pendingIntent = PendingIntent.getService(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            R.drawable.ic_stop,
            "Stop",
            pendingIntent
        )
    }

    private fun createPauseAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioRecordingForegroundService::class.java).apply {
            action = AudioRecordingForegroundService.ACTION_PAUSE_RECORDING
        }
        val pendingIntent = PendingIntent.getService(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            R.drawable.ic_pause,
            "Pause",
            pendingIntent
        )
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun hideRecordingNotification() {
        cancelNotification(NotificationIds.RECORDING)
    }

    override fun updateNotificationForPausedState() {
        val notification = createBaseNotification(CHANNEL_ID_RECORDING)
            .setContentTitle("Recording paused")
            .setContentText("Tap to view")
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .addAction(createStopAction())
            .addAction(createResumeAction())
            .build()

        notificationManager.notify(NotificationIds.RECORDING, notification)
    }

    override fun updateNotificationForRecordingState() {
        showRecordingNotification()
    }

    private fun createResumeAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioRecordingForegroundService::class.java).apply {
            action = AudioRecordingForegroundService.ACTION_RESUME_RECORDING
        }
        val pendingIntent = PendingIntent.getService(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            R.drawable.ic_play,
            "Resume",
            pendingIntent
        )
    }
} 