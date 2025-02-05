package com.example.clicknote.domain.service

import android.media.AudioFormat
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioService {
    suspend fun startRecording(outputFile: File)
    suspend fun stopRecording(): Result<File>
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun isRecording(): Boolean
    fun getAmplitude(): Int
    fun getAudioFormat(): AudioFormat
    fun getWaveformData(): Flow<FloatArray>
    suspend fun cleanup()
    suspend fun cancelRecording()
    fun getDuration(): Long
    fun getRecordingState(): Flow<RecordingState>
}

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    ERROR
} 