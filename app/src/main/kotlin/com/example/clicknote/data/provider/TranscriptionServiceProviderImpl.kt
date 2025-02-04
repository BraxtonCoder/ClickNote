package com.example.clicknote.data.provider

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.state.ActiveServiceState
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionServiceProviderImpl @Inject constructor(
    private val activeServiceState: Provider<ActiveServiceState>,
    private val serviceSelector: Provider<TranscriptionServiceSelector>
) : TranscriptionServiceProvider {

    override fun getServiceForSettings(context: TranscriptionServiceContext): TranscriptionCapable {
        val service = serviceSelector.get().selectService(context)
        activeServiceState.get().setActiveService(service)
        return service
    }

    override fun getActiveService(): TranscriptionCapable? =
        activeServiceState.get().activeService.value

    override suspend fun cleanup() {
        activeServiceState.get().activeService.value?.cleanup()
        activeServiceState.get().clearActiveService()
    }
} 
