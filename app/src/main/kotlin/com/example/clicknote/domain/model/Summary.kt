package com.example.clicknote.domain.model

data class Summary(
    val id: String,
    val content: String,
    val wordCount: Int,
    val sourceWordCount: Int,
    val keyPoints: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val sentiment: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) 