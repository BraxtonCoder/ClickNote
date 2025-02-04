package com.example.clicknote.data.mapper

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.mapper.ServiceStateEventMapper
import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.state.ServiceStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.example.clicknote.di.ApplicationScope
import dagger.Lazy
import dagger.Provider

@Singleton
class ServiceStateEventMapperImpl @Inject constructor(
    private val stateManager: Provider<ServiceStateManager>,
    private val eventBus: Provider<ServiceEventBus>,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : ServiceStateEventMapper {
    private var stateObservingJob: Job? = null

    override fun startObserving() {
        if (stateObservingJob == null) {
            stateObservingJob = coroutineScope.launch {
                stateManager.get().state
                    .onEach { state ->
                        when (state) {
                            is ServiceState.Active -> {
                                eventBus.get().emit(ServiceEvent.ServiceActivated(state.service.id))
                            }
                            is ServiceState.Idle -> {
                                eventBus.get().emit(ServiceEvent.ServiceReleased(""))
                            }
                            is ServiceState.Error -> {
                                eventBus.get().emit(ServiceEvent.ServiceError(state.error.message ?: "", state.error))
                            }
                        }
                    }
                    .launchIn(this)
            }
        }
    }

    override fun stopObserving() {
        stateObservingJob?.cancel()
        stateObservingJob = null
    }
} 