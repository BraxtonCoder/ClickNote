package com.example.clicknote.domain.mapper

import com.example.clicknote.domain.model.ServiceEvent
import com.example.clicknote.domain.model.ServiceState
import kotlinx.coroutines.flow.Flow

interface ServiceStateEventMapper {
    fun mapStateToEvents(state: Flow<ServiceState>): Flow<ServiceEvent>
} 