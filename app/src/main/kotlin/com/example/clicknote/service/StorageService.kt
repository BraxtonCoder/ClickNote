package com.example.clicknote.service

import java.io.File

interface StorageService {
    suspend fun saveAudioFile(file: File): String
    suspend fun getAudioFile(path: String): File
    suspend fun deleteAudioFile(path: String)
    suspend fun getAudioFileSize(path: String): Long
    suspend fun cleanup()
} 