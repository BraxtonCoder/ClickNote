package com.example.clicknote.data.state

import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ServiceStateManager that manages service state transitions
 */
@Singleton
class ServiceStateManagerImpl @Inject constructor() : ServiceStateManager {
    private val _currentState = MutableStateFlow<ServiceState>(ServiceState.Initialized(""))
    override val currentState: Flow<ServiceState> = _currentState.asStateFlow()

    override suspend fun updateState(state: ServiceState) {
        _currentState.value = state
    }

    override suspend fun getCurrentState(): ServiceState {
        return _currentState.value
    }

    override suspend fun reset() {
        _currentState.value = ServiceState.Initialized("")
    }

    override suspend fun cleanup() {
        _currentState.value = ServiceState.Cleaned("")
    }
} 
