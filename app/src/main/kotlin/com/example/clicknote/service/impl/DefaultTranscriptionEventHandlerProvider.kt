package com.example.clicknote.service.impl

import com.example.clicknote.domain.provider.TranscriptionEventHandlerProvider
import com.example.clicknote.service.TranscriptionEventHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTranscriptionEventHandlerProvider @Inject constructor(
    private val eventHandler: TranscriptionEventHandler
) : TranscriptionEventHandlerProvider {
    override fun provide(): TranscriptionEventHandler = eventHandler
} 