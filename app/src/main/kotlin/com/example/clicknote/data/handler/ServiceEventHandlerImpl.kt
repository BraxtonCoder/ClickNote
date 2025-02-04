package com.example.clicknote.data.handler

import com.example.clicknote.domain.handler.ServiceEventHandler
import com.example.clicknote.domain.service.Service
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.registry.ServiceRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceEventHandlerImpl @Inject constructor(
    private val serviceRegistry: ServiceRegistry,
    private val scope: CoroutineScope
) : ServiceEventHandler {

    private val _currentService = MutableStateFlow<TranscriptionCapable?>(null)
    override val currentService: StateFlow<TranscriptionCapable?> = _currentService.asStateFlow()

    override suspend fun handleServiceActivated(service: Service) {
        if (service !is TranscriptionCapable) return
        
        scope.launch(Dispatchers.IO) {
            try {
                val existingService = _currentService.value
                if (existingService != null && existingService.id != service.id) {
                    existingService.cleanup()
                }
                
                if (!service.isInitialized()) {
                    service.initialize()
                }
                
                _currentService.value = service
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    override suspend fun handleServiceDeactivated(service: Service) {
        scope.launch(Dispatchers.IO) {
            try {
                if (_currentService.value?.id == service.id) {
                    service.cleanup()
                    _currentService.value = null
                }
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    override suspend fun handleServiceError(service: Service, error: Throwable) {
        scope.launch(Dispatchers.IO) {
            try {
                if (_currentService.value?.id == service.id) {
                    service.cleanup()
                    _currentService.value = null
                }
            } catch (e: Exception) {
                // Log error but don't propagate to avoid infinite error loop
            }
        }
    }

    override suspend fun handleServiceInitialized(service: Service) {
        if (service !is TranscriptionCapable) return
        
        scope.launch(Dispatchers.IO) {
            try {
                if (_currentService.value?.id == service.id) {
                    _currentService.value = service
                }
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }

    override suspend fun handleServiceCleanup(service: Service) {
        scope.launch(Dispatchers.IO) {
            try {
                if (_currentService.value?.id == service.id) {
                    _currentService.value = null
                }
            } catch (e: Exception) {
                handleServiceError(service, e)
            }
        }
    }
}