package com.example.clicknote.service.impl

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.service.NetworkChecker
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinedTranscriptionService @Inject constructor(
    @Online private val onlineService: TranscriptionService,
    @Offline private val offlineService: TranscriptionService,
    private val networkChecker: NetworkChecker
) : TranscriptionService {

    private fun selectService(): TranscriptionService =
        if (networkChecker.isNetworkAvailable()) onlineService else offlineService

    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String> =
        selectService().transcribeAudio(audioData, settings)

    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String> =
        selectService().transcribeFile(file, settings)

    override suspend fun detectLanguage(audioData: ByteArray): Result<String> =
        selectService().detectLanguage(audioData)

    override suspend fun getAvailableLanguages(): Result<List<String>> =
        selectService().getAvailableLanguages()

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> =
        selectService().detectSpeakers(audioData)

    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> =
        selectService().identifySpeakers(audioData)

    override suspend fun generateSummary(text: String, template: SummaryTemplate?): Result<Summary> =
        selectService().generateSummary(text, template)

    override suspend fun cleanup() {
        onlineService.cleanup()
        offlineService.cleanup()
    }

    override fun isInitialized(): Boolean =
        onlineService.isInitialized() && offlineService.isInitialized()
} 