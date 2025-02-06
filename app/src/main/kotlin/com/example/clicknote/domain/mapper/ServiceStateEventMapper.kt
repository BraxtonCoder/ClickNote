package com.example.clicknote.domain.mapper

import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.state.ServiceState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for mapping service states to service events
 */
interface ServiceStateEventMapper {
    /**
     * Maps a flow of service states to a flow of service events
     * @param state The flow of service states to map
     * @return A flow of corresponding service events
     */
    fun mapStateToEvents(state: Flow<ServiceState>): Flow<ServiceEvent>
} 