package com.example.clicknote.data.state

import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.state.ActiveServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveServiceStateImpl @Inject constructor() : ActiveServiceState {
    private val _activeService = MutableStateFlow<TranscriptionCapable?>(null)
    override val activeService: StateFlow<TranscriptionCapable?> = _activeService.asStateFlow()

    override fun setActiveService(service: TranscriptionCapable?) {
        _activeService.value = service
    }

    override fun clearActiveService() {
        _activeService.value = null
    }
} 