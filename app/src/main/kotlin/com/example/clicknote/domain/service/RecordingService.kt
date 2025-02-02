package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface RecordingService {
    fun startRecording(outputFile: File)
    fun stopRecording(): Result<File>
    fun pauseRecording()
    fun resumeRecording()
    fun isRecording(): Boolean
    fun isPaused(): Boolean
    fun getAmplitude(): Flow<Float>
    fun getDuration(): Flow<Long>
    fun getRecordingState(): Flow<RecordingState>
    fun getOutputFile(): File?
    fun cancelRecording()
    fun release()
}

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED,
    ERROR
} 