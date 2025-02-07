package com.example.clicknote.di

import com.example.clicknote.data.repository.TranscriptionRepositoryImpl
import com.example.clicknote.data.repository.FolderRepositoryImpl
import com.example.clicknote.data.repository.UserRepositoryImpl
import com.example.clicknote.data.repository.CloudSyncRepositoryImpl
import com.example.clicknote.data.repository.BillingRepositoryImpl
import com.example.clicknote.data.repository.NoteRepositoryImpl
import com.example.clicknote.data.repository.AuthRepositoryImpl
import com.example.clicknote.data.repository.SearchHistoryRepositoryImpl
import com.example.clicknote.data.repository.SpeakerRepositoryImpl
import com.example.clicknote.data.repository.TranscriptionTimestampRepositoryImpl
import com.example.clicknote.data.repository.SpeakerProfileRepositoryImpl
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.repository.FolderRepository
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.domain.repository.CloudSyncRepository
import com.example.clicknote.domain.repository.BillingRepository
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.AuthRepository
import com.example.clicknote.domain.repository.SearchHistoryRepository
import com.example.clicknote.domain.repository.SpeakerRepository
import com.example.clicknote.domain.repository.TranscriptionTimestampRepository
import com.example.clicknote.domain.repository.SpeakerProfileRepository
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
    abstract fun bindTranscriptionRepository(
        impl: TranscriptionRepositoryImpl
    ): TranscriptionRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindCloudSyncRepository(
        impl: CloudSyncRepositoryImpl
    ): CloudSyncRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: BillingRepositoryImpl
    ): BillingRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        impl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSpeakerRepository(
        impl: SpeakerRepositoryImpl
    ): SpeakerRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptionTimestampRepository(
        impl: TranscriptionTimestampRepositoryImpl
    ): TranscriptionTimestampRepository

    @Binds
    @Singleton
    abstract fun bindSpeakerProfileRepository(
        impl: SpeakerProfileRepositoryImpl
    ): SpeakerProfileRepository
}