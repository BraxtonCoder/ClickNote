package com.example.clicknote.data.state

import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.di.qualifiers.Primary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ServiceStateManagerImpl @Inject constructor(
    @Primary private val primaryService: Provider<TranscriptionCapable>
) : ServiceStateManager {
    private val _state = MutableStateFlow<ServiceState>(ServiceState.Idle)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()

    private val _currentService = MutableStateFlow<TranscriptionCapable?>(null)
    override val currentService: StateFlow<TranscriptionCapable?> = _currentService.asStateFlow()

    init {
        // Initialize with primary service
        _currentService.value = primaryService.get()
        _state.value = ServiceState.Active(primaryService.get())
    }

    override suspend fun activateService(service: TranscriptionCapable) {
        _currentService.value = service
        _state.value = ServiceState.Active(service)
    }

    override suspend fun deactivateService() {
        // Reset to primary service when deactivating
        val primary = primaryService.get()
        _currentService.value = primary
        _state.value = ServiceState.Active(primary)
    }

    override fun getCurrentService(): TranscriptionCapable? = currentService.value
} 
