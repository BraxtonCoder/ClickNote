package com.example.clicknote.data.provider

import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.state.ActiveServiceState
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionServiceProviderImpl @Inject constructor(
    private val activeServiceState: Provider<ActiveServiceState>
) : TranscriptionServiceProvider {

    override fun setService(service: TranscriptionCapable?) {
        activeServiceState.get().setActiveService(service)
    }

    override fun getActiveService(): TranscriptionCapable? =
        activeServiceState.get().activeService.value

    override fun clearService() {
        activeServiceState.get().clearActiveService()
    }
} 
