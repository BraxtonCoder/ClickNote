package com.example.clicknote.domain.event

interface ServiceEventDispatcher {
    suspend fun dispatch(event: ServiceEvent)
} 