package com.example.clicknote.data.event

import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.example.clicknote.di.InternalEventFlow

@Singleton
class ServiceEventBusImpl @Inject constructor(
    @InternalEventFlow private val eventFlow: MutableSharedFlow<ServiceEvent>
) : ServiceEventBus {
    override val events: SharedFlow<ServiceEvent> = eventFlow.asSharedFlow()

    override suspend fun emit(event: ServiceEvent) {
        eventFlow.emit(event)
    }
}
