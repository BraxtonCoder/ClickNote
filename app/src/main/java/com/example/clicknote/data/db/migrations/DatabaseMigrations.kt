package com.example.clicknote.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create notes table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notes (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                folder_id TEXT,
                is_pinned INTEGER NOT NULL DEFAULT 0,
                has_audio INTEGER NOT NULL DEFAULT 0,
                audio_path TEXT,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                deleted_at INTEGER,
                summary TEXT,
                key_points TEXT NOT NULL,
                speakers TEXT NOT NULL,
                source TEXT NOT NULL,
                duration INTEGER,
                transcription_segments TEXT NOT NULL,
                FOREIGN KEY(folder_id) REFERENCES folders(id) ON DELETE SET NULL
            )
        """)

        // Create folders table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS folders (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                color INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                deleted_at INTEGER
            )
        """)

        // Create search_history table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS search_history (
                id TEXT NOT NULL PRIMARY KEY,
                query TEXT NOT NULL,
                type TEXT NOT NULL,
                result_count INTEGER NOT NULL,
                last_used INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                use_count INTEGER NOT NULL DEFAULT 1
            )
        """)

        // Create transcription_timestamps table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS transcription_timestamps (
                id TEXT NOT NULL PRIMARY KEY,
                note_id TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                text TEXT NOT NULL,
                speaker TEXT,
                confidence REAL NOT NULL,
                FOREIGN KEY(note_id) REFERENCES notes(id) ON DELETE CASCADE
            )
        """)

        // Create indices
        database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_folder_id ON notes(folder_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_created_at ON notes(created_at)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_is_deleted ON notes(is_deleted)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_folders_is_deleted ON folders(is_deleted)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_type ON search_history(type)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_last_used ON search_history(last_used)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_transcription_timestamps_note_id ON transcription_timestamps(note_id)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add sort_order column to folders table
        database.execSQL("ALTER TABLE folders ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0")
        
        // Create index for sort_order to optimize sorting queries
        database.execSQL("CREATE INDEX IF NOT EXISTS index_folders_sort_order ON folders(sort_order)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add sync_status column to folders table
        database.execSQL("ALTER TABLE folders ADD COLUMN sync_status INTEGER NOT NULL DEFAULT 0")
        
        // Create index for sync_status
        database.execSQL("CREATE INDEX IF NOT EXISTS index_folders_sync_status ON folders(sync_status)")

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
                is_deleted INTEGER NOT NULL DEFAULT 0,
                deleted_at INTEGER,
                sync_status INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Create indices for call_recordings table
        database.execSQL("CREATE INDEX IF NOT EXISTS index_call_recordings_created_at ON call_recordings(created_at)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_call_recordings_phone_number ON call_recordings(phone_number)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_call_recordings_sync_status ON call_recordings(sync_status)")
    }
} 