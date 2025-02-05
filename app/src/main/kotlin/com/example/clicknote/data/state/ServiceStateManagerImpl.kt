package com.example.clicknote.data.state

import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceStateManagerImpl @Inject constructor() : ServiceStateManager {
    private val _state = MutableStateFlow<ServiceState?>(null)
    override val state: StateFlow<ServiceState?> = _state.asStateFlow()

    private val _currentService = MutableStateFlow<TranscriptionCapable?>(null)
    override val currentService: StateFlow<TranscriptionCapable?> = _currentService.asStateFlow()

    override suspend fun updateState(serviceId: String, state: ServiceState) {
        _state.value = state
    }

    override suspend fun getState(serviceId: String): ServiceState? {
        return _state.value
    }

    override suspend fun clearState(serviceId: String) {
        _state.value = null
        if (_currentService.value?.id == serviceId) {
            _currentService.value = null
        }
    }

    override suspend fun activateService(service: TranscriptionCapable) {
        _currentService.value = service
        _state.value = ServiceState.Active(service)
    }

    override suspend fun deactivateService() {
        _currentService.value = null
        _state.value = ServiceState.Idle
    }

    override fun getCurrentService(): TranscriptionCapable? = _currentService.value
} 
