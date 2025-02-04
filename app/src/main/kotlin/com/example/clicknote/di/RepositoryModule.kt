package com.example.clicknote.di

import com.example.clicknote.domain.repository.*
import com.example.clicknote.repository.*
import com.example.clicknote.data.repository.TranscriptionRepositoryImpl
import com.example.clicknote.data.repository.TranscriptionTimestampRepositoryImpl
import com.example.clicknote.domain.repository.TranscriptionTimestampRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionRepository(impl: TranscriptionRepositoryImpl): TranscriptionRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindCloudSyncRepository(impl: CloudSyncRepositoryImpl): CloudSyncRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSpeakerRepository(impl: SpeakerRepositoryImpl): SpeakerRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptionSegmentRepository(impl: TranscriptionSegmentRepositoryImpl): TranscriptionSegmentRepository

    @Binds
    @Singleton
    abstract fun bindCallRecordingRepository(impl: CallRecordingRepositoryImpl): CallRecordingRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptionTimestampRepository(
        impl: TranscriptionTimestampRepositoryImpl
    ): TranscriptionTimestampRepository
}