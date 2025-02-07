package com.example.clicknote.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.data.dao.*
import com.example.clicknote.data.entity.*

private const val DATABASE_VERSION = 1
private const val DB_NAME = "clicknote.db"

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        TranscriptionSegmentEntity::class,
        TranscriptionMetadataEntity::class,
        SearchHistoryEntity::class,
        SubscriptionEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun transcriptionSegmentDao(): TranscriptionSegmentDao
    abstract fun transcriptionMetadataDao(): TranscriptionMetadataDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        const val DATABASE_NAME = DB_NAME
    }
} 