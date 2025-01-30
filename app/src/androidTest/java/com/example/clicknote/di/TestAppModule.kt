package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.data.FolderRepository
import com.example.clicknote.data.NoteRepository
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.AudioRecorderService
import com.example.clicknote.service.TranscriptionService
import com.example.clicknote.service.WhisperModel
import com.example.clicknote.di.PreferencesModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PreferencesModule::class]
)
object TestAppModule {
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferencesDataStore {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(@ApplicationContext context: Context): NoteRepository {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideFolderRepository(@ApplicationContext context: Context): FolderRepository {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideAudioRecorderService(@ApplicationContext context: Context): AudioRecorderService {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideTranscriptionService(@ApplicationContext context: Context): TranscriptionService {
        return mockk(relaxed = true)
    }

    @Provides
    @Singleton
    fun provideWhisperModel(@ApplicationContext context: Context): WhisperModel {
        return mockk(relaxed = true)
    }
} 