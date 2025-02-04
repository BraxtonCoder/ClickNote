package com.example.clicknote.data.lifecycle

import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.strategy.ServiceStrategy
import com.example.clicknote.domain.model.TranscriptionServiceContext
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Provider

@Singleton
class ServiceLifecycleManagerImpl @Inject constructor(
    private val strategy: Provider<ServiceStrategy>,
    private val registry: Provider<ServiceRegistry>,
    private val eventBus: Provider<ServiceEventBus>
) : ServiceLifecycleManager {

    override suspend fun initializeService(context: TranscriptionServiceContext) {
        strategy.get().determineServiceType(context).let { serviceType ->
            registry.get().getService(serviceType)?.let { service ->
                service.initialize(context)
                eventBus.get().emit(ServiceEvent.ServiceInitialized(service.id, context))
            }
        }
    }

    override suspend fun activateService(serviceId: String) {
        registry.get().getService(serviceId)?.let { service ->
            service.activate()
            eventBus.get().emit(ServiceEvent.ServiceActivated(service.id))
        }
    }

    override suspend fun deactivateService(serviceId: String) {
        registry.get().getService(serviceId)?.let { service ->
            service.deactivate()
            eventBus.get().emit(ServiceEvent.ServiceReleased(service.id))
        }
    }
} 