package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.clicknote.data.converter.RoomConverters
import java.time.LocalDateTime

@Entity(tableName = "search_history")
@TypeConverters(RoomConverters::class)
data class SearchHistory(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "query")
    val query: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "use_count")
    val useCount: Int = 0,

    @ColumnInfo(name = "last_used")
    val lastUsed: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

enum class SearchType {
    GLOBAL,        // Search across all notes
    NOTE_SPECIFIC, // Search within a specific note
    FOLDER_SPECIFIC // Search within a specific folder
} 