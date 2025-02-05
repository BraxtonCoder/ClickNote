package com.example.clicknote.data.lifecycle

import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.service.Service
import com.example.clicknote.domain.service.ActivatableService
import com.example.clicknote.domain.handler.ServiceEventHandler
import com.example.clicknote.domain.registry.ServiceRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceLifecycleManagerImpl @Inject constructor(
    private val serviceRegistry: ServiceRegistry,
    private val eventHandler: ServiceEventHandler,
    @ApplicationScope private val scope: CoroutineScope
) : ServiceLifecycleManager {

    override suspend fun initializeService(type: ServiceType) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getService(type) as? Service
                    ?: throw IllegalStateException("Service not found for type: $type")

                if (!service.isInitialized.first()) {
                    service.initialize()
                    eventHandler.handleServiceInitialized(service)
                }
            } catch (e: Exception) {
                handleServiceError(type.name, e)
            }
        }
    }

    override suspend fun activateService(serviceId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getService(serviceId) as? Service
                    ?: throw IllegalStateException("Service not found with id: $serviceId")

                if (service !is ActivatableService) {
                    throw IllegalStateException("Service $serviceId is not activatable")
                }

                if (!service.isInitialized.first()) {
                    service.initialize()
                    eventHandler.handleServiceInitialized(service)
                }

                service.activate()
                eventHandler.handleServiceActivated(service)
            } catch (e: Exception) {
                handleServiceError(serviceId, e)
            }
        }
    }

    override suspend fun deactivateService(serviceId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getService(serviceId) as? Service
                    ?: throw IllegalStateException("Service not found with id: $serviceId")

                if (service !is ActivatableService) {
                    throw IllegalStateException("Service $serviceId is not activatable")
                }

                service.deactivate()
                eventHandler.handleServiceDeactivated(service)
            } catch (e: Exception) {
                handleServiceError(serviceId, e)
            }
        }
    }

    override suspend fun cleanupService(serviceId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getService(serviceId) as? Service
                    ?: throw IllegalStateException("Service not found with id: $serviceId")

                service.cleanup()
                eventHandler.handleServiceCleanup(service)
            } catch (e: Exception) {
                handleServiceError(serviceId, e)
            }
        }
    }

    override suspend fun handleServiceError(serviceId: String, error: Throwable) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getService(serviceId) as? Service
                if (service != null) {
                    eventHandler.handleServiceError(service, error)
                }
            } catch (e: Exception) {
                // Log error but don't recursively handle it
            }
        }
    }

    override suspend fun getService(serviceId: String): Service? {
        return serviceRegistry.getService(serviceId) as? Service
    }

    override suspend fun getService(type: ServiceType): Service? {
        return serviceRegistry.getService(type) as? Service
    }
} 