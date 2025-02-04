package com.example.clicknote.domain.model

data class NoteSummary(
    val noteId: String,
    val summary: String,
    val updatedAt: Long = System.currentTimeMillis()
) 