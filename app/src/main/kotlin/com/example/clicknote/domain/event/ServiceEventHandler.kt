package com.example.clicknote.domain.event

interface ServiceEventHandler {
    suspend fun handleEvent(event: ServiceEvent)
} 