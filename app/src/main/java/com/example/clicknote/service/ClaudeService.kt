package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface ClaudeService {
    val isAvailable: Flow<Boolean>
    
    /**
     * Complete text using Claude model
     */
    suspend fun complete(prompt: String): String

    /**
     * Transcribe audio using Claude
     */
    suspend fun transcribe(audioFile: File): Result<String>

    /**
     * Detect speakers in audio
     */
    suspend fun detectSpeakers(audioFile: File): Result<List<String>>

    /**
     * Generate a summary of text
     */
    suspend fun summarize(text: String): Result<String>

    /**
     * Generate summary using Claude model
     */
    suspend fun generateSummary(text: String, template: String? = null): Result<String>

    /**
     * Stream summary generation
     */
    fun streamSummary(text: String, template: String? = null): Flow<String>

    /**
     * Ask a question about the text
     */
    suspend fun askQuestion(text: String, question: String): Result<String>

    /**
     * Extract key points from text
     */
    suspend fun extractKeyPoints(text: String): Result<List<String>>

    fun isInitialized(): Boolean

    suspend fun generateText(prompt: String): String
    suspend fun cleanup()
} 