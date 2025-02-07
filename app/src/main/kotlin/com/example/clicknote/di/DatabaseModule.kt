package com.example.clicknote.di

import android.content.Context
import androidx.room.Room
import com.example.clicknote.data.AppDatabase
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.data.dao.*
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRoomConverters(gson: Gson): RoomConverters = RoomConverters(gson)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        converters: RoomConverters
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()

    @Provides
    @Singleton
    fun provideTranscriptionSegmentDao(database: AppDatabase): TranscriptionSegmentDao = 
        database.transcriptionSegmentDao()

    @Provides
    @Singleton
    fun provideTranscriptionMetadataDao(database: AppDatabase): TranscriptionMetadataDao = 
        database.transcriptionMetadataDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = 
        database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao = 
        database.subscriptionDao()
} 