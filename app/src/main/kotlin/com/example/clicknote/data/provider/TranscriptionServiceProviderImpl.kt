package com.example.clicknote.data.provider

import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.state.TranscriptionServiceState
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionServiceProviderImpl @Inject constructor(
    private val serviceSelector: Provider<TranscriptionServiceSelector>,
    private val serviceState: Provider<TranscriptionServiceState>
) : TranscriptionServiceProvider {

    override fun getServiceForSettings(context: TranscriptionServiceContext): TranscriptionCapable {
        return serviceSelector.get().selectService(context).also { 
            serviceState.get().setActiveService(it)
        }
    }

    override fun getActiveService(): TranscriptionCapable? = 
        serviceState.get().activeService.value

    override suspend fun cleanup() {
        serviceState.get().clearState()
    }
} 
