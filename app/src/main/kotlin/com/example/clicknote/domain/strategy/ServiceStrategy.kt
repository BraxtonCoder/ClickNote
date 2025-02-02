package com.example.clicknote.domain.strategy

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.model.ServiceType

interface ServiceStrategy {
    fun determineServiceType(context: TranscriptionServiceContext): ServiceType
    fun createServiceContext(requireOnline: Boolean = false): TranscriptionServiceContext
    suspend fun validateServiceContext(context: TranscriptionServiceContext): Boolean
} 