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

@Singleton
class BackupNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.backup_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.backup_notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBackupProgress(progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_backup)
            .setContentTitle(context.getString(R.string.backup_in_progress))
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        notificationManager.notify(BACKUP_PROGRESS_ID, notification)
    }

    fun showBackupComplete(filesCount: Int, totalSize: Long) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_backup_complete)
            .setContentTitle(context.getString(R.string.backup_complete))
            .setContentText(context.getString(
                R.string.backup_complete_description,
                filesCount,
                formatSize(totalSize)
            ))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(BACKUP_COMPLETE_ID, notification)
    }

    fun showBackupError(error: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_backup_error)
            .setContentTitle(context.getString(R.string.backup_error))
            .setContentText(error)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BACKUP_ERROR_ID, notification)
    }

    fun showBackupCleanupComplete(deletedCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cleanup_complete)
            .setContentTitle(context.getString(R.string.backup_cleanup_complete))
            .setContentText(context.getString(
                R.string.backup_cleanup_complete_description,
                deletedCount
            ))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BACKUP_CLEANUP_COMPLETE_ID, notification)
    }

    fun showBackupCleanupError(error: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cleanup_error)
            .setContentTitle(context.getString(R.string.backup_cleanup_error))
            .setContentText(error)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BACKUP_CLEANUP_ERROR_ID, notification)
    }

    fun showRestoreProgress(progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_restore)
            .setContentTitle("Restoring Backup")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(RESTORE_PROGRESS_ID, notification)
    }

    fun showRestoreComplete(filesCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_restore_complete)
            .setContentTitle("Restore Complete")
            .setContentText("$filesCount files restored")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.cancel(RESTORE_PROGRESS_ID)
        notificationManager.notify(RESTORE_COMPLETE_ID, notification)
    }

    fun showRestoreError(error: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_restore_error)
            .setContentTitle("Restore Failed")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.cancel(RESTORE_PROGRESS_ID)
        notificationManager.notify(RESTORE_ERROR_ID, notification)
    }

    fun clearAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> String.format("%d B", bytes)
        }
    }

    companion object {
        private const val CHANNEL_ID = "backup_notifications"
        private const val BACKUP_PROGRESS_ID = 1
        private const val BACKUP_COMPLETE_ID = 2
        private const val BACKUP_ERROR_ID = 3
        private const val BACKUP_CLEANUP_COMPLETE_ID = 4
        private const val BACKUP_CLEANUP_ERROR_ID = 5
        private const val RESTORE_PROGRESS_ID = 6
        private const val RESTORE_COMPLETE_ID = 7
        private const val RESTORE_ERROR_ID = 8
    }
} 