package com.example.clicknote.domain.audio

import java.io.File

interface AudioConverter {
    suspend fun convertToWav(inputFile: File, outputFile: File): Result<File>
    suspend fun convertToMp3(inputFile: File, outputFile: File): Result<File>
    suspend fun getAudioDuration(file: File): Long
    suspend fun normalizeAudio(inputFile: File, outputFile: File): Result<File>
    suspend fun trimAudio(inputFile: File, outputFile: File, startMs: Long, endMs: Long): Result<File>
    suspend fun changeSpeed(inputFile: File, outputFile: File, speed: Float): Result<File>
} 