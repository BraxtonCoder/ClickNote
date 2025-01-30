package com.example.clicknote.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from schema version 1 to 2
 * Changes:
 * - Added duration column to notes table
 * - Renamed isInTrash to isDeleted in notes and folders tables
 * - Added updatedAt column to notes and folders tables
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add duration column to notes table
        database.execSQL(
            "ALTER TABLE notes ADD COLUMN duration INTEGER"
        )

        // Create temporary notes table with new schema
        database.execSQL("""
            CREATE TABLE notes_temp (
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

        // Copy data from old table to new table
        database.execSQL("""
            INSERT INTO notes_temp (
                id, title, content, created_at, updated_at, folder_id,
                is_pinned, has_audio, audio_path, is_deleted, deleted_at,
                summary, key_points, speakers, source, duration, transcription_segments
            )
            SELECT
                id, title, content, created_at, created_at, folder_id,
                is_pinned, has_audio, audio_path,
                CASE WHEN is_in_trash = 1 THEN 1 ELSE 0 END,
                deleted_at, summary, key_points, speakers, source,
                NULL, transcription_segments
            FROM notes
        """)

        // Drop old table and rename new table
        database.execSQL("DROP TABLE notes")
        database.execSQL("ALTER TABLE notes_temp RENAME TO notes")

        // Create temporary folders table with new schema
        database.execSQL("""
            CREATE TABLE folders_temp (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                color INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                deleted_at INTEGER
            )
        """)

        // Copy data from old table to new table
        database.execSQL("""
            INSERT INTO folders_temp (
                id, name, color, created_at, updated_at,
                is_deleted, deleted_at
            )
            SELECT
                id, name, color, created_at, created_at,
                CASE WHEN is_in_trash = 1 THEN 1 ELSE 0 END,
                deleted_at
            FROM folders
        """)

        // Drop old table and rename new table
        database.execSQL("DROP TABLE folders")
        database.execSQL("ALTER TABLE folders_temp RENAME TO folders")

        // Create index for folder_id in notes table
        database.execSQL("CREATE INDEX index_notes_folder_id ON notes(folder_id)")
    }
}

/**
 * Migration from schema version 2 to 3
 * Changes:
 * - Added search_history table
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE search_history (
                id TEXT NOT NULL PRIMARY KEY,
                query TEXT NOT NULL,
                type TEXT NOT NULL,
                result_count INTEGER NOT NULL,
                last_used INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                use_count INTEGER NOT NULL DEFAULT 1
            )
        """)
    }
}

/**
 * Migration from schema version 3 to 4
 * Changes:
 * - Added transcription_timestamps table
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE transcription_timestamps (
                id TEXT NOT NULL PRIMARY KEY,
                note_id TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                text TEXT NOT NULL,
                speaker TEXT,
                confidence REAL NOT NULL,
                FOREIGN KEY(note_id) REFERENCES notes(id) ON DELETE CASCADE
            )
        """)

        database.execSQL("CREATE INDEX index_transcription_timestamps_note_id ON transcription_timestamps(note_id)")
    }
} 