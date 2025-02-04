package com.example.clicknote.data.handler

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.domain.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider
import com.example.clicknote.di.ApplicationScope
import com.example.clicknote.di.InternalEventFlow

@Singleton
class ServiceEventHandlerImpl @Inject constructor(
    private val stateManager: Provider<ServiceStateManager>,
    private val registry: Provider<ServiceRegistry>,
    @InternalEventFlow private val events: Provider<SharedFlow<ServiceEvent>>,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : ServiceEventHandler {

    init {
        observeEvents()
    }

    private fun observeEvents() {
        events.get()
            .onEach { event -> 
                kotlinx.coroutines.runBlocking { handleEvent(event) }
            }
            .launchIn(coroutineScope)
    }

    override suspend fun handleEvent(event: ServiceEvent) {
        when (event) {
            is ServiceEvent.ServiceInitialized -> handleServiceInitialized(event)
            is ServiceEvent.ServiceActivated -> handleServiceActivated(event)
            is ServiceEvent.ServiceReleased -> handleServiceReleased(event)
            is ServiceEvent.ServiceError -> handleServiceError(event)
            is ServiceEvent.AllServicesReleased -> handleAllServicesReleased()
        }
    }

    private suspend fun handleServiceInitialized(event: ServiceEvent.ServiceInitialized) {
        val service = registry.get().getServiceById(event.serviceId)
        service?.let {
            val currentService = stateManager.get().getCurrentService()
            if (currentService?.id == service.id) {
                stateManager.get().activateService(service)
            }
        }
    }

    private suspend fun handleServiceActivated(event: ServiceEvent.ServiceActivated) {
        val service = registry.get().getServiceById(event.serviceId)
        service?.let { stateManager.get().activateService(it) }
    }

    private suspend fun handleServiceReleased(event: ServiceEvent.ServiceReleased) {
        stateManager.get().deactivateCurrentService()
    }

    private suspend fun handleServiceError(event: ServiceEvent.ServiceError) {
        // Handle service error, possibly notify UI or retry logic
    }

    private suspend fun handleAllServicesReleased() {
        stateManager.get().deactivateCurrentService()
    }
}