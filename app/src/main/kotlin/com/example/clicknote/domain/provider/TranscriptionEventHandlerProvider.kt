package com.example.clicknote.domain.provider

import com.example.clicknote.service.TranscriptionEventHandler

interface TranscriptionEventHandlerProvider {
    fun provide(): TranscriptionEventHandler
} 