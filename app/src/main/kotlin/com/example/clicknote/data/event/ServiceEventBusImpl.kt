package com.example.clicknote.data.event

import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceEventBusImpl @Inject constructor(
    private val _events: MutableSharedFlow<ServiceEvent>
) : ServiceEventBus {
    override val events: SharedFlow<ServiceEvent> = _events.asSharedFlow()

    override suspend fun emit(event: ServiceEvent) {
        _events.emit(event)
    }
} 
