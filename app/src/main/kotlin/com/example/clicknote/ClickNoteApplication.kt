package com.example.clicknote

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.clicknote.data.mapper.ServiceStateEventMapper
import com.example.clicknote.worker.CloudBackupWorker
import com.example.clicknote.worker.WeeklyTranscriptionResetWorker
import com.google.firebase.FirebaseApp
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import com.example.clicknote.domain.analytics.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class ClickNoteApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var serviceStateEventMapperProvider: Provider<ServiceStateEventMapper>

    @Inject
    lateinit var analyticsServiceProvider: Provider<AnalyticsService>

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val MIXPANEL_TOKEN = "a96f70206257896eabf7625522d7c8c9"
        private var instance: ClickNoteApplication? = null

        fun getInstance(): ClickNoteApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        applicationScope.launch {
            initializeComponents()
        }
    }

    private suspend fun initializeComponents() {
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize MixPanel
        MixpanelAPI.getInstance(this, MIXPANEL_TOKEN)

        // Setup WorkManager tasks
        setupWeeklyTranscriptionReset()
        setupCloudBackup()

        // Start observing service state changes
        serviceStateEventMapperProvider.get().startObserving()

        // Initialize analytics tracking
        analyticsServiceProvider.get().trackEvent("App Initialized")
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

    private fun setupWeeklyTranscriptionReset() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val weeklyWorkRequest = PeriodicWorkRequestBuilder<WeeklyTranscriptionResetWorker>(
            7, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "weekly_transcription_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                weeklyWorkRequest
            )
    }

    private fun setupCloudBackup() {
        CloudBackupWorker.schedule(this)
    }

    override fun onTerminate() {
        instance = null
        super.onTerminate()
    }
} 