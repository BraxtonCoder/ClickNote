package com.example.clicknote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.SettingsRepository
import com.example.clicknote.ui.settings.BackupSchedule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val cloudSyncRepository: CloudSyncRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Check if auto backup is enabled
            val settings = settingsRepository.getSettings()
            if (!settings.isAutoBackupEnabled) {
                return Result.success()
            }

            // Check if we're online and not in offline mode
            val isOfflineMode = cloudSyncRepository.isOfflineModeEnabled().first()
            if (isOfflineMode) {
                return Result.retry()
            }

            // Perform the backup
            cloudSyncRepository.syncNow()
            return Result.success()
        } catch (e: Exception) {
            return if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val WORK_NAME = "auto_backup_work"

        fun schedule(context: Context, schedule: BackupSchedule) {
            val workManager = WorkManager.getInstance(context)

            // Cancel any existing backup work
            workManager.cancelUniqueWork(WORK_NAME)

            // Create constraints
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            // Create periodic work request
            val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                schedule.intervalHours.toLong(),
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            // Enqueue unique periodic work
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
} 