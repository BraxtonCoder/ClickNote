package com.example.clicknote.data.mediator

import com.example.clicknote.domain.mediator.ServiceMediator
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEvent
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class ServiceMediatorImpl @Inject constructor(
    private val eventBus: Lazy<ServiceEventBus>
) : ServiceMediator {
    override suspend fun initializeService(service: TranscriptionService, context: TranscriptionServiceContext) {
        eventBus.get().emit(ServiceEvent.ServiceInitialized(service.id, context))
    }

    override suspend fun releaseService(service: TranscriptionService) {
        eventBus.get().emit(ServiceEvent.ServiceReleased(service.id))
    }

    override suspend fun releaseAll() {
        eventBus.get().emit(ServiceEvent.AllServicesReleased)
    }
} 