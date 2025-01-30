package com.example.clicknote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.clicknote.analytics.AnalyticsManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.CallRecordingRepository
import com.example.clicknote.service.cloud.CloudStorageService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class CloudBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CallRecordingRepository,
    private val cloudStorageService: CloudStorageService,
    private val userPreferences: UserPreferencesDataStore,
    private val analyticsManager: AnalyticsManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Check if cloud sync is enabled
        if (!userPreferences.isCloudSyncEnabled().first()) {
            return Result.success()
        }

        return try {
            // Get all recordings that need to be synced
            val recordings = repository.getAllCallRecordings().first()

            // Start sync
            analyticsManager.trackCloudSyncStarted(recordings.size)
            setProgress(workDataOf(KEY_PROGRESS to "Syncing recordings"))

            // Perform sync
            cloudStorageService.syncCallRecordings(recordings).getOrThrow()

            // Track success
            analyticsManager.trackCloudSyncCompleted(recordings.size)
            Result.success()
        } catch (e: Exception) {
            // Track error
            analyticsManager.trackCloudSyncError(e.message ?: "Unknown error")
            
            // Retry only if it's a network error or authentication error
            if (e is java.net.UnknownHostException || e.message?.contains("auth", ignoreCase = true) == true) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_PROGRESS = "progress"
        private const val WORK_NAME = "cloud_backup"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(
                repeatInterval = 24, // Sync every 24 hours
                repeatIntervalTimeUnit = java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
} 