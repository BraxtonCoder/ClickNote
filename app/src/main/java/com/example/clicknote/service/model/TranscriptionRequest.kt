package com.example.clicknote.service.model

import java.io.File
import com.example.clicknote.domain.model.Language

data class TranscriptionRequest(
    val audioFile: File,
    val language: Language? = null,
    val prompt: String? = null,
    val temperature: Float = 0.0f,
    val responseFormat: String = "json"
) 