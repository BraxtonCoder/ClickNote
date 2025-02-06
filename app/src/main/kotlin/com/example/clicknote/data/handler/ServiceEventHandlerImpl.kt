package com.example.clicknote.data.handler

import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.domain.service.Service
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEvent.*
import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.domain.model.TranscriptionServiceContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceEventHandlerImpl @Inject constructor(
    private val stateManager: ServiceStateManager,
    private val serviceRegistry: ServiceRegistry,
    private val eventFlow: SharedFlow<ServiceEvent>,
    @ApplicationScope private val scope: CoroutineScope
) : ServiceEventHandler {

    private val _currentService = MutableStateFlow<TranscriptionCapable?>(null)
    override val currentService: Flow<TranscriptionCapable?> = _currentService.asStateFlow()

    private val _serviceEvents = MutableSharedFlow<ServiceEvent>()

    override suspend fun handleServiceActivated(service: Service) {
        scope.launch {
            try {
                if (service is TranscriptionCapable) {
                    _currentService.value = service
                    stateManager.updateState(ServiceState.Active(service.id))
                    _serviceEvents.emit(ServiceActivated(service.id))
                }
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    override suspend fun handleServiceDeactivated(service: Service) {
        scope.launch {
            try {
                if (_currentService.value?.id == service.id) {
                    _currentService.value = null
                    stateManager.updateState(ServiceState.Inactive(service.id))
                    _serviceEvents.emit(ServiceDeactivated(service.id))
                }
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    override suspend fun handleServiceError(service: Service, error: Throwable) {
        scope.launch {
            try {
                if (_currentService.value?.id == service.id) {
                    _currentService.value = null
                }
                stateManager.updateState(ServiceState.Error(service.id, error))
                _serviceEvents.emit(ServiceError(service.id, error))
            } catch (e: Exception) {
                // Log error but don't recursively handle it
            }
        }
    }

    override suspend fun handleServiceInitialized(service: Service) {
        scope.launch {
            try {
                if (service is TranscriptionCapable) {
                    service.initialize()
                    stateManager.updateState(ServiceState.Initialized(service.id))
                    val context = TranscriptionServiceContext()
                    _serviceEvents.emit(ServiceInitialized(service.id, context))
                }
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    override suspend fun handleServiceCleanup(service: Service) {
        scope.launch {
            try {
                if (_currentService.value?.id == service.id) {
                    _currentService.value = null
                }
                service.cleanup()
                stateManager.updateState(ServiceState.Cleaned(service.id))
                _serviceEvents.emit(ServiceCleaned(service.id))
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }
}