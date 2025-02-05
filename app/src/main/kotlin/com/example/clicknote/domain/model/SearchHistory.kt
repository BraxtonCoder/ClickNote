package com.example.clicknote.domain.model

data class SearchHistory(
    val id: String,
    val query: String,
    val type: SearchType,
    val useCount: Int,
    val timestamp: Long
) 