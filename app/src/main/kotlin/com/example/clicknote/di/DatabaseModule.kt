package com.example.clicknote.di

import android.content.Context
import androidx.room.Room
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.data.db.ClickNoteDatabase
import com.example.clicknote.data.dao.*
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
    fun provideRoomConverters(): RoomConverters {
        return RoomConverters()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        roomConverters: RoomConverters
    ): ClickNoteDatabase {
        return Room.databaseBuilder(
            context,
            ClickNoteDatabase::class.java,
            ClickNoteDatabase.DATABASE_NAME
        )
        .addTypeConverter(roomConverters)
        .addMigrations(
            ClickNoteDatabase.Migrations.MIGRATION_1_2,
            ClickNoteDatabase.Migrations.MIGRATION_2_3,
            ClickNoteDatabase.Migrations.MIGRATION_3_4,
            ClickNoteDatabase.Migrations.MIGRATION_4_5
        )
        .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: ClickNoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: ClickNoteDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: ClickNoteDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideTranscriptionTimestampDao(database: ClickNoteDatabase): TranscriptionTimestampDao {
        return database.transcriptionTimestampDao()
    }

    @Provides
    @Singleton
    fun provideTranscriptionMetadataDao(database: ClickNoteDatabase): TranscriptionMetadataDao {
        return database.transcriptionMetadataDao()
    }

    @Provides
    @Singleton
    fun provideSpeakerProfileDao(database: ClickNoteDatabase): SpeakerProfileDao {
        return database.speakerProfileDao()
    }

    @Provides
    @Singleton
    fun provideCallRecordingDao(database: ClickNoteDatabase): CallRecordingDao {
        return database.callRecordingDao()
    }
} 