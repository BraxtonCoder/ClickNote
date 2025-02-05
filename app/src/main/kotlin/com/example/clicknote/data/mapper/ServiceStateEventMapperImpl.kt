package com.example.clicknote.data.mapper

import com.example.clicknote.domain.mapper.ServiceStateEventMapper
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.model.ServiceState
import com.example.clicknote.domain.model.TranscriptionServiceContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceStateEventMapperImpl @Inject constructor() : ServiceStateEventMapper {
    override fun mapStateToEvents(state: Flow<ServiceState>): Flow<ServiceEvent> {
        return state.map { serviceState ->
            when (serviceState) {
                is ServiceState.Initialized -> ServiceEvent.ServiceInitialized(
                    serviceId = serviceState.serviceId,
                    context = TranscriptionServiceContext()
                )
                is ServiceState.Active -> ServiceEvent.ServiceActivated(serviceState.serviceId)
                is ServiceState.Inactive -> ServiceEvent.ServiceReleased(serviceState.serviceId)
                is ServiceState.Error -> ServiceEvent.ServiceError(serviceState.serviceId, serviceState.error)
                is ServiceState.Cleaned -> ServiceEvent.AllServicesReleased
            }
        }
    }
} 