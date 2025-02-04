package com.example.clicknote.data.strategy

import com.example.clicknote.domain.strategy.ServiceStrategy
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.model.ServiceType
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.event.ServiceEvent
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider

@Singleton
class ServiceStrategyImpl @Inject constructor(
    private val eventBus: Provider<ServiceEventBus>
) : ServiceStrategy {

    override fun determineServiceType(context: TranscriptionServiceContext): ServiceType {
        return if (shouldUseOnlineService(context)) {
            ServiceType.ONLINE
        } else {
            ServiceType.OFFLINE
        }
    }

    private fun shouldUseOnlineService(context: TranscriptionServiceContext): Boolean {
        return context.isNetworkAvailable && context.isOnlineTranscriptionEnabled
    }

    override fun createServiceContext(requireOnline: Boolean): TranscriptionServiceContext =
        TranscriptionServiceContext(
            settings = TranscriptionSettings(
                preferOfflineMode = !requireOnline,
                isNetworkAvailable = true
            ),
            requiresOnline = requireOnline
        )

    override suspend fun validateServiceContext(context: TranscriptionServiceContext): Boolean {
        return when {
            context.requiresOnline && !context.settings.isNetworkAvailable -> {
                eventBus.get().emit(ServiceEvent.ServiceError("", IllegalStateException("Network unavailable for online service")))
                false
            }
            !context.requiresOnline && !context.settings.preferOfflineMode -> {
                eventBus.get().emit(ServiceEvent.ServiceError("", IllegalStateException("Offline service not initialized")))
                false
            }
            else -> true
        }
    }
} 