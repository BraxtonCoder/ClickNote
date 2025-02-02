package com.example.clicknote.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.clicknote.domain.model.TranscriptionLanguage

interface PreferencesRepository {
    suspend fun setOpenAIApiKey(apiKey: String)
    suspend fun getOpenAIApiKey(): String
    suspend fun setTranscriptionLanguage(language: TranscriptionLanguage)
    fun getTranscriptionLanguage(): Flow<TranscriptionLanguage>
    suspend fun setOnlineTranscriptionEnabled(enabled: Boolean)
    fun isOnlineTranscriptionEnabled(): Flow<Boolean>
    suspend fun setAudioSavingEnabled(enabled: Boolean)
    fun isAudioSavingEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun areNotificationsEnabled(): Flow<Boolean>
    suspend fun setAutoSummaryEnabled(enabled: Boolean)
    fun isAutoSummaryEnabled(): Flow<Boolean>
    suspend fun setTheme(theme: String)
    fun getTheme(): Flow<String>
    suspend fun clear()
} 