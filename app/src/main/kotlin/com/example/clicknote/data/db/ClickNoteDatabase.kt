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
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4,
                Migrations.MIGRATION_4_5
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

    object Migrations {
        val MIGRATION_1_2 = object : Migration(1, 2) {
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop old note_entity table
                database.execSQL("DROP TABLE IF EXISTS note_entity")

                // Create new note_entity table with updated schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS note_entity (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT,
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        is_pinned INTEGER NOT NULL DEFAULT 0,
                        has_audio INTEGER NOT NULL DEFAULT 0,
                        audio_path TEXT,
                        source TEXT NOT NULL DEFAULT 'MANUAL',
                        folder_id TEXT,
                        summary TEXT,
                        key_points TEXT NOT NULL DEFAULT '[]',
                        speakers TEXT NOT NULL DEFAULT '[]',
                        sync_status TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY(folder_id) REFERENCES folder_entity(id) ON DELETE SET NULL
                    )
                """)

                // Create indices for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_entity_folder_id ON note_entity(folder_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_entity_created_at ON note_entity(created_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_entity_updated_at ON note_entity(updated_at)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_note_entity_is_deleted ON note_entity(is_deleted)")
            }
        }
    }
} 