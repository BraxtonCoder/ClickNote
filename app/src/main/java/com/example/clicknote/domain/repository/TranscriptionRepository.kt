package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionResult

interface TranscriptionRepository {
    suspend fun saveTranscription(transcriptionResult: TranscriptionResult)
    suspend fun getTranscriptions(): List<TranscriptionResult>
    suspend fun getTranscriptionById(id: String): TranscriptionResult
    suspend fun deleteTranscription(id: String)
    suspend fun saveTranscriptionAudio(id: String, audioBytes: ByteArray): String
    suspend fun deleteTranscriptionAudio(id: String)
} 