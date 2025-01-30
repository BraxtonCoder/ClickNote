package com.example.clicknote

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.clicknote.worker.CloudBackupWorker
import com.example.clicknote.worker.WeeklyTranscriptionResetWorker
import com.google.firebase.FirebaseApp
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

@HiltAndroidApp
class ClickNoteApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        private const val MIXPANEL_TOKEN = "your_mixpanel_token_here"
        private const val USER_PREFERENCES = "user_preferences"
        val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES)
    }

    override fun onCreate() {
        super.onCreate()
        setupWeeklyTranscriptionReset()
        setupCloudBackup()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize MixPanel
        MixpanelAPI.getInstance(this, MIXPANEL_TOKEN)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
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
} 