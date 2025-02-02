package com.example.clicknote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.clicknote.service.BackupService
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.service.BackupNotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduledBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupService: BackupService,
    private val userRepository: UserRepository,
    private val notificationService: BackupNotificationService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!userRepository.isSignedIn()) {
            return Result.success()
        }

        var progress = 0
        setProgress(workDataOf("progress" to progress))

        return try {
            backupService.createBackup(
                name = "scheduled_backup",
                onProgress = { newProgress ->
                    progress = newProgress
                    setProgress(workDataOf("progress" to progress))
                    notificationService.showBackupProgress(progress)
                }
            ).collect { result ->
                when (result) {
                    is com.example.clicknote.service.BackupResult.Success -> {
                        notificationService.showBackupComplete(1, 0)
                        return Result.success()
                    }
                    is com.example.clicknote.service.BackupResult.Error -> {
                        notificationService.showBackupError(result.message)
                        return Result.retry()
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            notificationService.showBackupError(e.message ?: "Backup failed")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "scheduled_backup"

        fun schedule(context: Context, intervalHours: Int) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .build()

            val request = PeriodicWorkRequestBuilder<ScheduledBackupWorker>(
                intervalHours.toLong(),
                TimeUnit.HOURS,
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
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

        fun updateInterval(context: Context, intervalHours: Int) {
            cancel(context)
            schedule(context, intervalHours)
        }
    }
} 