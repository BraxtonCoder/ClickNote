package com.example.clicknote.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.data.dao.*
import com.example.clicknote.data.entity.*
import com.example.clicknote.data.db.migrations.DatabaseMigrations
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        SearchHistory::class,
        TranscriptionTimestamp::class,
        TranscriptionMetadata::class,
        SpeakerProfile::class,
        CallRecordingEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class ClickNoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun transcriptionTimestampDao(): TranscriptionTimestampDao
    abstract fun transcriptionMetadataDao(): TranscriptionMetadataDao
    abstract fun speakerProfileDao(): SpeakerProfileDao
    abstract fun callRecordingDao(): CallRecordingDao

    companion object {
        const val DATABASE_NAME = "clicknote.db"

        @Volatile
        private var INSTANCE: ClickNoteDatabase? = null

        fun getInstance(context: Context): ClickNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ClickNoteDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ClickNoteDatabase::class.java,
                DATABASE_NAME
            )
            .addMigrations(
                DatabaseMigrations.MIGRATION_1_2,
                DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4,
                DatabaseMigrations.MIGRATION_4_5
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Add any initial data here if needed
                }
            })
            .build()
        }
    }
} 