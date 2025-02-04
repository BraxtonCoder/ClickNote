package com.example.clicknote.data.registry

import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.service.TranscriptionCapable
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
    
    override fun getService(serviceId: String): TranscriptionCapable? =
        when (serviceId) {
            onlineService.get().id -> onlineService.get()
            offlineService.get().id -> offlineService.get()
            else -> null
        }

    override fun getService(serviceType: ServiceType): TranscriptionCapable? =
        when (serviceType) {
            ServiceType.ONLINE -> onlineService.get()
            ServiceType.OFFLINE -> offlineService.get()
            ServiceType.COMBINED -> null // TODO: Implement combined service if needed
            else -> null
        }

    override fun getOnlineService(): OnlineCapableService = onlineService.get()

    override fun getOfflineService(): OfflineCapableService = offlineService.get()
} 
