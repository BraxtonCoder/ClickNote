package com.example.clicknote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeeklyTranscriptionResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferences: UserPreferencesDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            userPreferences.resetWeeklyTranscriptionCount()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 