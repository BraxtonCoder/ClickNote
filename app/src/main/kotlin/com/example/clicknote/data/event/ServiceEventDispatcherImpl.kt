package com.example.clicknote.data.event

import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class ServiceEventDispatcherImpl @Inject constructor(
    private val eventBus: Lazy<ServiceEventBus>
) : ServiceEventDispatcher {
    override suspend fun dispatchEvent(event: ServiceEvent) {
        eventBus.get().emit(event)
    }
} 