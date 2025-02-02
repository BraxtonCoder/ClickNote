package com.example.clicknote.data.lifecycle

import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.strategy.ServiceStrategy
import com.example.clicknote.domain.model.TranscriptionServiceContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceLifecycleManagerImpl @Inject constructor(
    private val strategy: ServiceStrategy,
    private val registry: ServiceRegistry,
    private val eventBus: ServiceEventBus
) : ServiceLifecycleManager {

    override suspend fun initializeService(context: TranscriptionServiceContext) {
        strategy.determineServiceType(context).let { serviceType ->
            registry.getService(serviceType)?.let { service ->
                service.initialize(context)
                eventBus.emit(ServiceEvent.ServiceInitialized(service.id, context))
            }
        }
    }

    override suspend fun activateService(serviceId: String) {
        registry.getService(serviceId)?.let { service ->
            service.activate()
            eventBus.emit(ServiceEvent.ServiceActivated(service.id))
        }
    }

    override suspend fun deactivateService(serviceId: String) {
        registry.getService(serviceId)?.let { service ->
            service.deactivate()
            eventBus.emit(ServiceEvent.ServiceReleased(service.id))
        }
    }
} 