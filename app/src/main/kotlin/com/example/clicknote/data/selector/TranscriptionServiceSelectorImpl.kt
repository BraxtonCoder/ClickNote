package com.example.clicknote.data.selector

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionServiceSelectorImpl @Inject constructor(
    private val onlineService: TranscriptionCapable,
    private val offlineService: TranscriptionCapable
) : TranscriptionServiceSelector {

    override fun selectService(context: TranscriptionServiceContext): TranscriptionCapable {
        return when {
            // If online is required and conditions are met, use online service
            context.requireOnline && context.isNetworkAvailable && context.isOnlineTranscriptionEnabled -> onlineService
            
            // If offline mode is preferred, use offline service
            context.preferOfflineMode -> offlineService
            
            // If network is not available or online transcription is disabled, use offline service
            !context.isNetworkAvailable || !context.isOnlineTranscriptionEnabled -> offlineService
            
            // Default to online service if all conditions are favorable
            else -> onlineService
        }
    }

    override fun isServiceAvailable(service: TranscriptionCapable, context: TranscriptionServiceContext): Boolean {
        return when (service) {
            onlineService -> context.isNetworkAvailable && context.isOnlineTranscriptionEnabled
            offlineService -> true
            else -> false
        }
    }
} 