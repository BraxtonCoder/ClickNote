package com.example.clicknote.service.audio

import kotlinx.coroutines.flow.Flow
import java.io.File
import com.example.clicknote.domain.model.RecordingState

interface AudioService {
    suspend fun startRecording(outputFile: File)
    suspend fun stopRecording()
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun getAmplitude(): Int
    fun getRecordingState(): Flow<RecordingState>
    suspend fun enhanceAudio(inputFile: File, outputFile: File)
    suspend fun convertAudio(inputFile: File, outputFile: File, format: AudioFileFormat)
    suspend fun playAudio(file: File)
    suspend fun stopPlayback()
}

enum class AudioFileFormat {
    WAV,
    MP3,
    M4A,
    FLAC
} 