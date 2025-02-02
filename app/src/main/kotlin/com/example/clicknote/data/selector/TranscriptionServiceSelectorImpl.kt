package com.example.clicknote.data.selector

import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.factory.TranscriptionServiceFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TranscriptionServiceSelectorImpl @Inject constructor(
    private val serviceFactory: Provider<TranscriptionServiceFactory>
) : TranscriptionServiceSelector {

    override fun selectService(context: TranscriptionServiceContext): TranscriptionCapable {
        return when {
            context.requiresOnline -> serviceFactory.get().createOnlineService()
            context.allowFallback -> serviceFactory.get().createCombinedService()
            else -> serviceFactory.get().createOfflineService()
        }
    }

    override fun getOnlineService(): TranscriptionCapable =
        serviceFactory.get().createOnlineService()

    override fun getOfflineService(): TranscriptionCapable =
        serviceFactory.get().createOfflineService()
} 