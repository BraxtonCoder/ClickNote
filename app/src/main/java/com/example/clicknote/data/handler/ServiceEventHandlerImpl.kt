package com.example.clicknote.data.handler

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceEventHandlerImpl @Inject constructor(
    stateManager: ServiceStateManager,
    registry: ServiceRegistry,
    private val events: Flow<ServiceEvent>,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : ServiceEventHandler {

    private val stateManager by lazy { stateManager }
    private val registry by lazy { registry }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        events
            .onEach { event -> handleEvent(event) }
            .launchIn(coroutineScope)
    }

    override suspend fun handleEvent(event: ServiceEvent) {
        when (event) {
            is ServiceEvent.ServiceInitialized -> handleServiceInitialized(event)
            is ServiceEvent.ServiceActivated -> handleServiceActivated(event)
            is ServiceEvent.ServiceReleased -> handleServiceReleased(event)
            else -> Unit
        }
    }

    private suspend fun handleServiceInitialized(event: ServiceEvent.ServiceInitialized) {
        registry.getService(event.serviceId)?.let { service ->
            // Handle service initialization
        }
    }

    private suspend fun handleServiceActivated(event: ServiceEvent.ServiceActivated) {
        registry.getService(event.serviceId)?.let { service ->
            stateManager.activateService(service)
        }
    }

    private suspend fun handleServiceReleased(event: ServiceEvent.ServiceReleased) {
        stateManager.deactivateService()
    }
}