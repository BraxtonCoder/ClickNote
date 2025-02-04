package com.example.clicknote.data.lifecycle

import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.strategy.ServiceStrategy
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.model.Service
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider

@Singleton
class ServiceLifecycleManagerImpl @Inject constructor(
    private val strategy: Provider<ServiceStrategy>,
    private val registry: Provider<ServiceRegistry>,
    private val eventBus: Provider<ServiceEventBus>
) : ServiceLifecycleManager {

    override suspend fun initializeService(context: TranscriptionServiceContext) {
        val serviceType = strategy.get().determineServiceType(context)
        registry.get().getServiceByType(serviceType)?.let { service ->
            service.initialize(context)
            eventBus.get().emitEvent(ServiceEvent.ServiceInitialized(service.serviceId, context))
        }
    }

    override suspend fun activateService(serviceId: String) {
        registry.get().getServiceById(serviceId)?.let { service ->
            service.activate()
            eventBus.get().emitEvent(ServiceEvent.ServiceActivated(service.serviceId))
        }
    }

    override suspend fun deactivateService(serviceId: String) {
        registry.get().getServiceById(serviceId)?.let { service ->
            service.deactivate()
            eventBus.get().emitEvent(ServiceEvent.ServiceReleased(service.serviceId))
        }
    }
} 