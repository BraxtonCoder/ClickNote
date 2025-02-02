package com.example.clicknote.data.mapper

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.mapper.ServiceStateEventMapper
import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Lazy
import javax.inject.Singleton

@Singleton
class ServiceStateEventMapperImpl @Inject constructor(
    stateManager: Lazy<ServiceStateManager>,
    private val eventFlow: MutableSharedFlow<ServiceEvent>,
    private val coroutineScope: CoroutineScope
) : ServiceStateEventMapper {
    private var stateObservingJob: Job? = null
    private val stateManager by lazy { stateManager.get() }

    override fun startObserving() {
        stateObservingJob?.cancel()
        stateObservingJob = coroutineScope.launch {
            stateManager.state.collect { state ->
                when (state) {
                    is ServiceState.Active -> {
                        eventFlow.emit(ServiceEvent.ServiceActivated(state.service.id))
                    }
                    is ServiceState.Idle -> {
                        eventFlow.emit(ServiceEvent.ServiceReleased(""))
                    }
                    is ServiceState.Error -> {
                        eventFlow.emit(ServiceEvent.ServiceError("", state.error))
                    }
                }
            }
        }
    }

    override fun stopObserving() {
        stateObservingJob?.cancel()
        stateObservingJob = null
    }
} 