package com.example.clicknote.service.model

import java.io.File
import com.example.clicknote.domain.model.TranscriptionLanguage

data class TranscriptionRequest(
    val audioFile: File,
    val language: TranscriptionLanguage? = null,
    val prompt: String? = null,
    val temperature: Float = 0.0f,
    val responseFormat: String = "json"
) 