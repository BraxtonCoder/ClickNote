package com.example.clicknote.data.state

import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceStateManagerImpl @Inject constructor() : ServiceStateManager {
    private val _activeService = MutableStateFlow<TranscriptionService?>(null)
    override val activeService: StateFlow<TranscriptionService?> = _activeService.asStateFlow()

    override fun setActiveService(service: TranscriptionService?) {
        _activeService.value = service
    }

    override fun clearActiveService() {
        _activeService.value = null
    }

    override fun getActiveService(): TranscriptionService? = _activeService.value
} 
