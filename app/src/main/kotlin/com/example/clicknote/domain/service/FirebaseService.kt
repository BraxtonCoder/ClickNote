package com.example.clicknote.domain.service

import java.io.File

interface FirebaseService {
    suspend fun syncNote(noteId: String): Result<Unit>
    suspend fun uploadAudio(file: File, noteId: String): Result<String>
    suspend fun downloadAudio(url: String): Result<File>
    suspend fun deleteNote(noteId: String): Result<Unit>
    suspend fun deleteAudio(url: String): Result<Unit>
    suspend fun isAvailable(): Boolean
    suspend fun initialize()
    suspend fun cleanup()
} 