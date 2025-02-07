package com.example.clicknote.domain.model

interface SpeechModel : AutoCloseable {
    val path: String
    fun isLoaded(): Boolean
} 