package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.clicknote.data.converter.RoomConverters
import java.util.UUID

@Entity(tableName = "search_history")
@TypeConverters(RoomConverters::class)
data class SearchHistory(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "query")
    val query: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "result_count")
    val resultCount: Int = 0
)

enum class SearchType {
    GLOBAL,        // Search across all notes
    NOTE_SPECIFIC, // Search within a specific note
    FOLDER_SPECIFIC // Search within a specific folder
} 