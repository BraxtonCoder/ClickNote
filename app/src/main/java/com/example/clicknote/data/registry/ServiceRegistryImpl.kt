package com.example.clicknote.data.registry

import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.service.OnlineCapableService
import com.example.clicknote.domain.service.OfflineCapableService
import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRegistryImpl @Inject constructor(
    @Online private val onlineService: Lazy<OnlineCapableService>,
    @Offline private val offlineService: Lazy<OfflineCapableService>
) : ServiceRegistry {
    
    override fun getService(serviceId: String): TranscriptionService? =
        when (serviceId) {
            onlineService.get().id -> onlineService.get()
            offlineService.get().id -> offlineService.get()
            else -> null
        }

    override fun getService(serviceType: ServiceType): TranscriptionService? =
        when (serviceType) {
            ServiceType.ONLINE -> onlineService.get()
            ServiceType.OFFLINE -> offlineService.get()
        }

    override fun getOnlineService(): OnlineCapableService = onlineService.get()

    override fun getOfflineService(): OfflineCapableService = offlineService.get()
} 
