package com.example.clicknote.domain.strategy

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.data.strategy.TranscriptionMode
import java.io.File

interface ServiceStrategy {
    fun determineServiceType(context: TranscriptionServiceContext): ServiceType
    fun createServiceContext(requireOnline: Boolean = false): TranscriptionServiceContext
    suspend fun validateServiceContext(context: TranscriptionServiceContext): Boolean
    fun getService(mode: TranscriptionMode): TranscriptionCapable
    
    suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings,
        mode: TranscriptionMode = TranscriptionMode.AUTO
    ): Result<TranscriptionResult>
    
    suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings,
        mode: TranscriptionMode = TranscriptionMode.AUTO
    ): Result<TranscriptionResult>
} 