package com.example.clicknote.data.mapper

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.mapper.ServiceStateEventMapper
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import com.example.clicknote.di.ApplicationScope

@Singleton
class ServiceStateEventMapperImpl @Inject constructor(
    private val stateManager: Provider<ServiceStateManager>,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : ServiceStateEventMapper {
    private var stateObservingJob: Job? = null

    init {
        observeState()
    }

    private fun observeState() {
        stateObservingJob = coroutineScope.launch {
            stateManager.get().observeState()
                .onEach { state ->
                    when (state) {
                        is ServiceState.Active -> {
                            eventFlow.emit(ServiceEvent.ServiceActivated(state.service.id))
                        }
                        is ServiceState.Idle -> {
                            eventFlow.emit(ServiceEvent.ServiceReleased(""))
                        }
                        is ServiceState.Error -> {
                            eventFlow.emit(ServiceEvent.ServiceError(state.error.message ?: "", state.error))
                        }
                    }
                }
                .launchIn(this)
        }
    }

    override fun cleanup() {
        stateObservingJob?.cancel()
        stateObservingJob = null
    }
} 