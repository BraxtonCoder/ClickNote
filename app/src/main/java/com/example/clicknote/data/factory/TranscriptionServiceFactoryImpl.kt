package com.example.clicknote.data.factory

import com.example.clicknote.domain.factory.TranscriptionServiceFactory
import com.example.clicknote.domain.service.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import com.example.clicknote.di.qualifiers.Combined

@Singleton
class TranscriptionServiceFactoryImpl @Inject constructor(
    @Online private val onlineService: Provider<TranscriptionCapable>,
    @Offline private val offlineService: Provider<TranscriptionCapable>,
    @Combined private val combinedService: Provider<TranscriptionCapable>
) : TranscriptionServiceFactory {

    override fun createOnlineService(): TranscriptionCapable =
        onlineService.get()

    override fun createOfflineService(): TranscriptionCapable =
        offlineService.get()

    override fun createCombinedService(): TranscriptionCapable =
        combinedService.get()

    override suspend fun releaseService(service: Any) {
        when (service) {
            is TranscriptionCapable -> service.cleanup()
        }
    }
} 