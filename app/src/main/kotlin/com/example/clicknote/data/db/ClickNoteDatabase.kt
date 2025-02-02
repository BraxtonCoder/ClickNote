package com.example.clicknote.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.data.dao.*
import com.example.clicknote.data.entity.*
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
    version = 4,
    exportSchema = false
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Add any initial data here if needed
                }
            })
            .build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add sync_status column with default value of 0 (PENDING)
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 0"
                )
                // Create index for sync_status
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_notes_sync_status` ON notes(sync_status)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add sort_order column to folders table
                database.execSQL(
                    "ALTER TABLE folders ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0"
                )
                // Create index for sort_order
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_folders_sort_order` ON folders(sort_order)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add sync_status column to folders table
                database.execSQL(
                    "ALTER TABLE folders ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 0"
                )
                // Create index for sync_status
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_folders_sync_status` ON folders(sync_status)"
                )

                // Create call_recordings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS call_recordings (
                        id TEXT NOT NULL PRIMARY KEY,
                        phone_number TEXT NOT NULL,
                        contact_name TEXT,
                        duration INTEGER NOT NULL,
                        audio_path TEXT NOT NULL,
                        transcription TEXT,
                        summary TEXT,
                        is_incoming INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        is_deleted INTEGER NOT NULL,
                        deleted_at INTEGER,
                        sync_status INTEGER NOT NULL
                    )
                """)
                
                // Create indices for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_call_recordings_created_at ON call_recordings(created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_call_recordings_phone_number ON call_recordings(phone_number)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_call_recordings_sync_status ON call_recordings(sync_status)")
            }
        }
    }
} 