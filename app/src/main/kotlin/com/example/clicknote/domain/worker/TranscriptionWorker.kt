package com.example.clicknote.domain.worker

import java.io.File
import kotlinx.coroutines.flow.Flow

interface TranscriptionWorker {
    suspend fun transcribe(audioFile: File): Result<String>
    suspend fun transcribeWithTimestamps(audioFile: File): Result<List<TimestampedTranscription>>
    fun getTranscriptionProgress(): Flow<Float>
    fun cancelTranscription()
    suspend fun isTranscribing(): Boolean
}

data class TimestampedTranscription(
    val text: String,
    val startTime: Long,
    val endTime: Long
) 