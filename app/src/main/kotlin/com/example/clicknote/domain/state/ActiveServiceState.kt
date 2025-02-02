package com.example.clicknote.domain.state

import com.example.clicknote.domain.service.TranscriptionCapable
import kotlinx.coroutines.flow.StateFlow

interface ActiveServiceState {
    val activeService: StateFlow<TranscriptionCapable?>
    fun setActiveService(service: TranscriptionCapable?)
    fun clearActiveService()
} 