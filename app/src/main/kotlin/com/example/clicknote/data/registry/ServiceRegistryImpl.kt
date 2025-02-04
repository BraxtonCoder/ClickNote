package com.example.clicknote.data.registry

import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.service.OnlineCapableService
import com.example.clicknote.domain.service.OfflineCapableService
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
    private val services = mutableMapOf<String, TranscriptionService>()

    init {
        // Register default services
        onlineService.get().let { service ->
            services[service.id] = service as TranscriptionService
        }
        offlineService.get().let { service ->
            services[service.id] = service as TranscriptionService
        }
    }

    override fun getService(serviceId: String): TranscriptionService? {
        return services[serviceId]
    }

    override fun getService(serviceType: ServiceType): TranscriptionService? {
        return when (serviceType) {
            ServiceType.ONLINE -> onlineService.get() as TranscriptionService
            ServiceType.OFFLINE -> offlineService.get() as TranscriptionService
            else -> null
        }
    }

    override fun getOnlineService(): OnlineCapableService = onlineService.get()

    override fun getOfflineService(): OfflineCapableService = offlineService.get()

    override fun registerService(service: TranscriptionService) {
        services[service.id] = service
    }

    override fun unregisterService(serviceId: String) {
        services.remove(serviceId)
    }
}
