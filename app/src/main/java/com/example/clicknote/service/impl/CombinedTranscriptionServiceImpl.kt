package com.example.clicknote.service.impl

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.service.NetworkChecker
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CombinedTranscriptionServiceImpl @Inject constructor(
    @Online private val onlineService: TranscriptionCapable,
    @Offline private val offlineService: TranscriptionCapable,
    private val networkChecker: NetworkChecker
) : TranscriptionCapable {

    override val id: String = "combined_service"

    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.transcribeAudio(audioData, settings)
        } else {
            offlineService.transcribeAudio(audioData, settings)
        }
    }

    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.transcribeFile(file, settings)
        } else {
            offlineService.transcribeFile(file, settings)
        }
    }

    override suspend fun detectLanguage(audioData: ByteArray): Result<String> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.detectLanguage(audioData)
        } else {
            offlineService.detectLanguage(audioData)
        }
    }

    override suspend fun getAvailableLanguages(): Result<List<String>> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.getAvailableLanguages()
        } else {
            offlineService.getAvailableLanguages()
        }
    }

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.detectSpeakers(audioData)
        } else {
            offlineService.detectSpeakers(audioData)
        }
    }

    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.identifySpeakers(audioData)
        } else {
            offlineService.identifySpeakers(audioData)
        }
    }

    override suspend fun generateSummary(text: String, template: SummaryTemplate?): Result<Summary> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.generateSummary(text, template)
        } else {
            offlineService.generateSummary(text, template)
        }
    }

    override suspend fun cleanup() {
        onlineService.cleanup()
        offlineService.cleanup()
    }

    override fun isInitialized(): Boolean {
        return onlineService.isInitialized() && offlineService.isInitialized()
    }
}