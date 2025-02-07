package com.example.clicknote.di

import android.content.Context
import android.media.AudioManager
import androidx.work.WorkManager
import com.example.clicknote.analytics.AnalyticsTracker
import com.example.clicknote.analytics.MixPanelAnalyticsTracker
import com.example.clicknote.util.AudioFeatureExtractor
import com.example.clicknote.util.AudioFeatureExtractorImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides
    @Singleton
    fun provideAudioFeatureExtractor(
        @ApplicationContext context: Context
    ): AudioFeatureExtractor = AudioFeatureExtractorImpl(context)

    @Provides
    @Singleton
    fun provideAnalyticsTracker(
        @ApplicationContext context: Context
    ): AnalyticsTracker = MixPanelAnalyticsTracker(context)
} 