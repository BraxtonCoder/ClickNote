package com.example.clicknote.domain.service

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.state.ServiceState
import kotlinx.coroutines.flow.Flow

interface ServiceStateEventMapper {
    fun mapStateToEvent(state: ServiceState): ServiceEvent
    fun observeServiceEvents(): Flow<ServiceEvent>
    fun observeServiceState(): Flow<ServiceState>
    suspend fun updateServiceState(state: ServiceState)
    suspend fun emitServiceEvent(event: ServiceEvent)
} 