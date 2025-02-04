package com.example.clicknote.data.lifecycle

import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.service.Service
import com.example.clicknote.domain.handler.ServiceEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceLifecycleManagerImpl @Inject constructor(
    private val serviceRegistry: ServiceRegistry,
    private val eventHandler: ServiceEventHandler,
    private val scope: CoroutineScope
) : ServiceLifecycleManager {

    override suspend fun initializeService(type: ServiceType) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getServiceByType(type)
                if (!service.isInitialized()) {
                    service.initialize()
                }
                eventHandler.handleServiceInitialized(service)
            } catch (e: Exception) {
                handleServiceError(type.name, e)
            }
        }
    }

    override suspend fun activateService(id: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getServiceById(id)
                if (service != null) {
                    if (!service.isInitialized()) {
                        service.initialize()
                    }
                    eventHandler.handleServiceActivated(service)
                }
            } catch (e: Exception) {
                handleServiceError(id, e)
            }
        }
    }

    override suspend fun deactivateService(id: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getServiceById(id)
                if (service != null) {
                    eventHandler.handleServiceDeactivated(service)
                }
            } catch (e: Exception) {
                handleServiceError(id, e)
            }
        }
    }

    override suspend fun cleanupService(id: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getServiceById(id)
                if (service != null) {
                    service.cleanup()
                    eventHandler.handleServiceCleanup(service)
                }
            } catch (e: Exception) {
                handleServiceError(id, e)
            }
        }
    }

    override suspend fun handleServiceError(id: String, error: Throwable) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = serviceRegistry.getServiceById(id)
                if (service != null) {
                    eventHandler.handleServiceError(service, error)
                }
            } catch (e: Exception) {
                // Log error but don't propagate to avoid infinite error loop
            }
        }
    }
} 