package com.example.clicknote

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClickNoteApplication : Application(), androidx.work.Configuration.Provider {
    
    companion object {
        private const val MIXPANEL_TOKEN = "a96f70206257896eabf7625522d7c8c9"
        private const val MIN_BACKOFF_MILLIS = 30_000L // 30 seconds in milliseconds
        private const val MAX_BACKOFF_MILLIS = 7_200_000L // 2 hours in milliseconds
    }
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private lateinit var mixpanel: MixpanelAPI
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        
        // Initialize Mixpanel
        mixpanel = MixpanelAPI.getInstance(
            applicationContext,
            MIXPANEL_TOKEN,
            true
        )
        
        // Initialize WorkManager with custom configuration
        WorkManager.initialize(
            this,
            getWorkManagerConfiguration()
        )
    }
    
    override fun getWorkManagerConfiguration(): androidx.work.Configuration {
        return androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setDefaultProcessName("${packageName}.background")
            .setJobSchedulerJobIdRange(1000, 20000)
            .build()
    }
    
    override fun onTerminate() {
        mixpanel.flush()
        super.onTerminate()
    }
} 