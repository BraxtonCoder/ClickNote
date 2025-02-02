package com.example.clicknote.service.impl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.clicknote.R
import com.example.clicknote.domain.model.Note
import com.example.clicknote.ui.MainActivity
import com.example.clicknote.service.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationService {

    companion object {
        const val CHANNEL_ID_RECORDING = "recording_channel"
        const val CHANNEL_ID_TRANSCRIPTION = "transcription_channel"
        const val CHANNEL_ID_PREMIUM = "premium_channel"
        
        const val NOTIFICATION_ID_RECORDING = 1
        const val NOTIFICATION_ID_TRANSCRIPTION = 2
        const val NOTIFICATION_ID_PREMIUM = 3
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var recordingAmplitude: Float = 0f

    override fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createRecordingChannel()
            createTranscriptionChannel()
            createPremiumChannel()
        }
    }

    override fun createForegroundNotification(service: Service) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECORDING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_recording_title))
            .setContentText(context.getString(R.string.notification_recording_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        service.startForeground(NOTIFICATION_ID_RECORDING, notification)
    }

    override fun updateRecordingProgress(amplitude: Float) {
        recordingAmplitude = amplitude
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECORDING)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle("Recording in progress")
            .setContentText("Tap to return to app")
            .setOngoing(true)
            .setProgress(100, (amplitude * 100).toInt(), false)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID_RECORDING, notification)
    }

    override fun stopForegroundNotification(service: Service) {
        service.stopForeground(true)
    }

    override fun showTranscriptionNotification(note: Note) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", note.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSCRIPTION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_transcription_complete_title))
            .setContentText(note.content.take(100))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TRANSCRIPTION, notification)
    }

    override fun showRecordingNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECORDING)
            .setContentTitle(context.getString(R.string.notification_recording_title))
            .setContentText(context.getString(R.string.notification_recording_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_RECORDING, notification)
    }

    override fun showTranscribingNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSCRIPTION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_transcribing_title))
            .setContentText(context.getString(R.string.notification_transcribing_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TRANSCRIPTION, notification)
    }

    override fun hideRecordingNotification() {
        notificationManager.cancel(NOTIFICATION_ID_RECORDING)
    }

    override fun hideTranscribingNotification() {
        notificationManager.cancel(NOTIFICATION_ID_TRANSCRIPTION)
    }

    override fun showPremiumLimitNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openPremium", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PREMIUM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_premium_limit_title))
            .setContentText(context.getString(R.string.notification_premium_limit_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PREMIUM, notification)
    }

    private fun createRecordingChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_RECORDING,
                context.getString(R.string.channel_recording_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_recording_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createTranscriptionChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_TRANSCRIPTION,
                context.getString(R.string.channel_transcription_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_transcription_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPremiumChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_PREMIUM,
                context.getString(R.string.channel_premium_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_premium_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
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

    private fun createNotePendingIntent(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", note.id)
        }
        return PendingIntent.getActivity(
            context,
            note.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createCopyPendingIntent(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_COPY
            putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
            putExtra(MainActivity.EXTRA_CONTENT, note.content)
        }
        return PendingIntent.getActivity(
            context,
            note.id.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSharePendingIntent(note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHARE
            putExtra(MainActivity.EXTRA_NOTE_ID, note.id)
            putExtra(MainActivity.EXTRA_CONTENT, note.content)
        }
        return PendingIntent.getActivity(
            context,
            note.id.hashCode() + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createPremiumPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_premium", true)
        }
        return PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_PREMIUM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
} 