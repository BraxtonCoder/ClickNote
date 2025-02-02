package com.example.clicknote.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideTranscriptionWorker(impl: TranscriptionWorkerImpl): TranscriptionWorker {
        return impl
    }

    @Provides
    @Singleton
    fun provideSummaryWorker(impl: SummaryWorkerImpl): SummaryWorker {
        return impl
    }

    @Provides
    @Singleton
    fun provideAudioEnhancementWorker(impl: AudioEnhancementWorkerImpl): AudioEnhancementWorker {
        return impl
    }

    @Provides
    @Singleton
    fun provideCloudSyncWorker(impl: CloudSyncWorkerImpl): CloudSyncWorker {
        return impl
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
} 