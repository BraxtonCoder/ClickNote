package com.example.clicknote

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.clicknote.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClickNoteApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private lateinit var mixpanel: MixpanelAPI
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        
        // Initialize Stripe
        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )
        
        // Initialize Mixpanel
        mixpanel = MixpanelAPI.getInstance(
            applicationContext,
            BuildConfig.MIXPANEL_TOKEN,
            false  // optOut is false to enable analytics by default
        )
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setDefaultProcessName("${packageName}.background")
            .setJobSchedulerJobIdRange(1000, 20000)
            .build()
    
    override fun onTerminate() {
        mixpanel.flush()
        super.onTerminate()
    }
} 