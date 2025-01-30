package com.example.clicknote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.clicknote.domain.model.BackupSettings
import com.example.clicknote.service.BackupService
import com.example.clicknote.service.BackupNotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

@HiltWorker
class BackupCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupService: BackupService,
    private val notificationService: BackupNotificationService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val settings = backupService.getBackupSettings().first()
            val deletedCount = cleanupOldBackups(settings)
            
            if (deletedCount > 0) {
                notificationService.showBackupCleanupComplete(deletedCount)
            }
            
            return Result.success()
        } catch (e: Exception) {
            notificationService.showBackupCleanupError(e.message ?: "Cleanup failed")
            return Result.retry()
        }
    }

    private suspend fun cleanupOldBackups(settings: BackupSettings): Int {
        var deletedCount = 0
        val backups = backupService.listBackups()
        
        // Group backups by version
        val backupsByVersion = backups.groupBy { it.version }
        
        // Keep the latest version and clean up old versions beyond maxBackupCount
        backupsByVersion.entries
            .sortedByDescending { it.key }
            .drop(settings.maxBackupCount)
            .forEach { (_, backupsInVersion) ->
                backupsInVersion.forEach { backup ->
                    backupService.deleteBackup(backup.path)
                    deletedCount++
                }
            }
        
        return deletedCount
    }

    companion object {
        private const val WORK_NAME = "backup_cleanup"
        private const val DEFAULT_CLEANUP_INTERVAL_DAYS = 7L

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<BackupCleanupWorker>(
                DEFAULT_CLEANUP_INTERVAL_DAYS,
                TimeUnit.DAYS,
                6,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
} 