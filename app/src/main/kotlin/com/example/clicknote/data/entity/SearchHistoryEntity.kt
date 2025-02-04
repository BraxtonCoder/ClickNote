package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [
        Index("query"),
        Index("type"),
        Index("timestamp")
    ]
)
data class SearchHistoryEntity(
    @PrimaryKey
    val id: String,
    val query: String,
    val type: String,
    val useCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
) 