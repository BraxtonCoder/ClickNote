package com.example.clicknote.data

data class TranscriptionResult(
    val text: String,
    val speakers: List<String> = emptyList(),
    val timestamps: List<Long> = emptyList()
) 