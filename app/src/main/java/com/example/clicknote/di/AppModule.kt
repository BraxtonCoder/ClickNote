package com.example.clicknote.di

import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.data.service.DefaultTranscriptionService
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // MixPanel
    @Provides
    @Singleton
    fun provideMixpanel(@ApplicationContext context: Context): MixpanelAPI {
        return MixpanelAPI.getInstance(
            context,
            "a96f70206257896eabf7625522d7c8c9",
            true
        )
    }
    
    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
        return FirebaseApp.initializeApp(context) 
            ?: throw IllegalStateException("FirebaseApp initialization failed")
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(firebaseApp: FirebaseApp): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFunctions(firebaseApp: FirebaseApp): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }
    
    // WorkManager
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    // Json
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
            coerceInputValues = true
        }
    }
    
    // Transcription Service
    @Provides
    @Singleton
    fun provideTranscriptionService(service: DefaultTranscriptionService): TranscriptionCapable {
        return service
    }
} 