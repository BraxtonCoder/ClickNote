package com.example.clicknote.domain.service

import android.media.AudioFormat
import com.example.clicknote.domain.model.RecordingState
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Service interface for audio recording and playback functionality
 */
interface AudioService {
    val recordingState: Flow<RecordingState>
    val recordingAmplitude: Flow<Int>
    val recordingDuration: Flow<Long>
    val recordingError: Flow<String?>

    suspend fun startRecording(outputFile: File): Result<Unit>
    suspend fun stopRecording(): Result<File>
    suspend fun pauseRecording(): Result<Unit>
    suspend fun resumeRecording(): Result<Unit>
    suspend fun cancelRecording()
    suspend fun isRecording(): Boolean
    suspend fun cleanup()
    fun getAudioFormat(): AudioFormat
    fun getWaveformData(): Flow<FloatArray>
    fun getDuration(): Long
} 