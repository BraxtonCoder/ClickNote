package com.example.clicknote.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.clicknote.R
import com.example.clicknote.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.example.clicknote.domain.model.Note

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val CHANNEL_ID_USAGE = "usage_alerts"
        private const val NOTIFICATION_ID_USAGE_WARNING = 1001
        private const val NOTIFICATION_ID_USAGE_LIMIT = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val usageChannel = NotificationChannel(
                CHANNEL_ID_USAGE,
                "Usage Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about transcription usage limits"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(usageChannel)
        }
    }

    fun showUsageWarningNotification(usedCount: Int, totalLimit: Int) {
        val remainingCount = totalLimit - usedCount
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openSubscription", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_USAGE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Approaching Weekly Limit")
            .setContentText("You have $remainingCount transcriptions remaining this week")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You have used $usedCount of $totalLimit weekly transcriptions. " +
                        "Upgrade to Premium for unlimited transcriptions."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_upgrade,
                "Upgrade Now",
                pendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_USAGE_WARNING, notification)
    }

    fun showUsageLimitReachedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openSubscription", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_USAGE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Weekly Limit Reached")
            .setContentText("Upgrade to Premium for unlimited transcriptions")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You've reached your weekly transcription limit. " +
                        "Upgrade to Premium for unlimited transcriptions and more features."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_upgrade,
                "Upgrade Now",
                pendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_USAGE_LIMIT, notification)
    }

    fun clearUsageNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_USAGE_WARNING)
        notificationManager.cancel(NOTIFICATION_ID_USAGE_LIMIT)
    }

    fun createForegroundNotification(service: android.app.Service)
    fun updateRecordingProgress(amplitude: Float)
    fun stopForegroundNotification(service: android.app.Service)
    fun showTranscriptionNotification(note: Note)
    fun showRecordingNotification()
    fun showTranscribingNotification()
    fun hideRecordingNotification()
    fun hideTranscribingNotification()
    fun showPremiumLimitNotification()
    
    fun cancelNotification(id: Int)
    
    fun cancelAllNotifications()
} 