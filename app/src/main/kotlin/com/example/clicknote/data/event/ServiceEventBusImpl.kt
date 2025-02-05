package com.example.clicknote.data.event

import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.di.qualifiers.InternalEventFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceEventBusImpl @Inject constructor(
    @InternalEventFlow private val eventFlow: MutableSharedFlow<ServiceEvent>
) : ServiceEventBus {
    override val events: SharedFlow<ServiceEvent> = eventFlow

    override suspend fun emit(event: ServiceEvent) {
        eventFlow.emit(event)
    }
}
