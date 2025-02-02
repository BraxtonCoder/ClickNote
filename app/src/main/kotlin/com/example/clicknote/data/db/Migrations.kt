package com.example.clicknote.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create a temporary table with the new schema
        database.execSQL("""
            CREATE TABLE folders_temp (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                color INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                modified_at INTEGER NOT NULL,
                is_in_trash INTEGER NOT NULL,
                deleted_at INTEGER
            )
        """)

        // Copy data from the old table to the new table
        database.execSQL("""
            INSERT INTO folders_temp (
                id, name, color, created_at, modified_at, is_in_trash, deleted_at
            )
            SELECT 
                id, name, color, created_at, modified_at, is_deleted, deleted_at
            FROM folders
        """)

        // Drop the old table
        database.execSQL("DROP TABLE folders")

        // Rename the temporary table to the original name
        database.execSQL("ALTER TABLE folders_temp RENAME TO folders")
    }
} 