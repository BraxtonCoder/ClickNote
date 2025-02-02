package com.example.clicknote.domain.event

interface ServiceEventDispatcher {
    suspend fun dispatchEvent(event: ServiceEvent)
} 