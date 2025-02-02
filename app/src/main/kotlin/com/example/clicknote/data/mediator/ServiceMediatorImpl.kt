package com.example.clicknote.data.mediator

import com.example.clicknote.domain.mediator.ServiceMediator
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceMediatorImpl @Inject constructor(
    private val eventBus: ServiceEventBus
) : ServiceMediator {
    override suspend fun initializeService(service: TranscriptionService, context: TranscriptionServiceContext) {
        eventBus.emit(ServiceEvent.ServiceInitialized(service.id, context))
    }

    override suspend fun releaseService(service: TranscriptionService) {
        eventBus.emit(ServiceEvent.ServiceReleased(service.id))
    }

    override suspend fun releaseAll() {
        eventBus.emit(ServiceEvent.AllServicesReleased)
    }
} 