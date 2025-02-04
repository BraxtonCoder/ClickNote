package com.example.clicknote.data.handler

import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.domain.handler.ServiceEventHandler
import com.example.clicknote.domain.service.Service
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.model.ServiceEvent
import com.example.clicknote.domain.model.ServiceState
import com.example.clicknote.domain.registry.ServiceRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceEventHandlerImpl @Inject constructor(
    private val serviceRegistry: ServiceRegistry,
    @ApplicationScope private val scope: CoroutineScope
) : ServiceEventHandler {

    private val _currentService = MutableStateFlow<TranscriptionCapable?>(null)
    override val currentService: Flow<TranscriptionCapable?> = _currentService.asStateFlow()

    private val _serviceState = MutableStateFlow<ServiceState?>(null)
    private val _serviceEvents = MutableSharedFlow<ServiceEvent>()

    override suspend fun handleServiceActivated(service: Service) {
        scope.launch {
            try {
                if (service is TranscriptionCapable) {
                    _currentService.value = service
                }
                emitEvent(ServiceEvent.ServiceActivated(service.id))
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
                }
                emitEvent(ServiceEvent.ServiceDeactivated(service.id))
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
                emitEvent(ServiceEvent.ServiceError(service.id, error))
            } catch (e: Exception) {
                // Log error but don't recursively handle it
            }
        }
    }

    override suspend fun handleServiceInitialized(service: Service) {
        scope.launch {
            try {
                service.initialize()
                emitEvent(ServiceEvent.ServiceInitialized(service.id))
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
                emitEvent(ServiceEvent.ServiceCleaned(service.id))
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    private suspend fun emitEvent(event: ServiceEvent) {
        _serviceEvents.emit(event)
    }
}