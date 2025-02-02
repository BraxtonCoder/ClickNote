package com.example.clicknote.domain.handler

import com.example.clicknote.domain.event.ServiceEvent
import kotlinx.coroutines.flow.Flow

interface ServiceEventHandler {
    fun observeServiceEvents(): Flow<ServiceEvent>
    suspend fun handleEvent(event: ServiceEvent)
}