package com.example.clicknote.domain.registry

import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.service.OnlineCapableService
import com.example.clicknote.domain.service.OfflineCapableService
import com.example.clicknote.domain.model.ServiceType

interface ServiceRegistry {
    fun getService(serviceId: String): TranscriptionService?
    fun getService(serviceType: ServiceType): TranscriptionService?
    fun getOnlineService(): OnlineCapableService
    fun getOfflineService(): OfflineCapableService
} 