package com.example.clicknote.domain.audio

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
    fun pause()
    fun resume()
    fun isRecording(): Boolean
    fun getAmplitude(): Int
    fun getWaveformData(): Flow<FloatArray>
    fun getDuration(): Long
    fun cleanup()
} 