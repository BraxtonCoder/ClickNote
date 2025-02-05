package com.example.clicknote.data.provider

import com.example.clicknote.domain.model.TranscriptionServiceContext
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionServiceProviderImpl @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository,
    private val transcriptionSelector: TranscriptionServiceSelector,
    private val connectivityManager: NetworkConnectivityManager,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val onlineTranscriptionService: TranscriptionCapable,
    private val offlineTranscriptionService: TranscriptionCapable
) : TranscriptionServiceProvider {

    override fun getService(): TranscriptionCapable {
        return if (shouldUseOnlineService()) {
            onlineTranscriptionService
        } else {
            offlineTranscriptionService
        }
    }

    private fun shouldUseOnlineService(): Boolean = runBlocking {
        connectivityManager.isNetworkAvailable.first() && 
        preferencesDataStore.onlineTranscriptionEnabled.first()
    }

    override fun getServiceForSettings(context: TranscriptionServiceContext): TranscriptionCapable {
        return transcriptionSelector.selectService(context)
    }

    override fun getActiveService(): TranscriptionCapable? = null

    override suspend fun cleanup() {
        onlineTranscriptionService.cleanup()
        offlineTranscriptionService.cleanup()
    }
} 
