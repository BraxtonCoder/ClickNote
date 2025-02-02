package com.example.clicknote.service.impl

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.service.NetworkChecker
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.service.PerformanceMonitor
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CombinedTranscriptionServiceImpl @Inject constructor(
    @Online private val onlineService: Provider<TranscriptionCapable>,
    @Offline private val offlineService: Provider<TranscriptionCapable>,
    private val networkChecker: NetworkChecker,
    private val performanceMonitor: Provider<PerformanceMonitor>
) : TranscriptionCapable {

    override val id: String = "combined_service"

    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String> {
        performanceMonitor.get().startOperation("combined_transcribe")
        return try {
            if (networkChecker.isNetworkAvailable()) {
                onlineService.get().transcribeAudio(audioData, settings)
            } else {
                offlineService.get().transcribeAudio(audioData, settings)
            }
        } finally {
            performanceMonitor.get().endOperation("combined_transcribe")
        }
    }

    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String> {
        performanceMonitor.get().startOperation("combined_file_transcribe")
        return try {
            if (networkChecker.isNetworkAvailable()) {
                onlineService.get().transcribeFile(file, settings)
            } else {
                offlineService.get().transcribeFile(file, settings)
            }
        } finally {
            performanceMonitor.get().endOperation("combined_file_transcribe")
        }
    }

    override suspend fun detectLanguage(audioData: ByteArray): Result<String> {
        performanceMonitor.get().startOperation("combined_detect_language")
        return try {
            if (networkChecker.isNetworkAvailable()) {
                onlineService.get().detectLanguage(audioData)
            } else {
                offlineService.get().detectLanguage(audioData)
            }
        } finally {
            performanceMonitor.get().endOperation("combined_detect_language")
        }
    }

    override suspend fun getAvailableLanguages(): Result<List<String>> {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.get().getAvailableLanguages()
        } else {
            offlineService.get().getAvailableLanguages()
        }
    }

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> {
        performanceMonitor.get().startOperation("combined_detect_speakers")
        return try {
            if (networkChecker.isNetworkAvailable()) {
                onlineService.get().detectSpeakers(audioData)
            } else {
                offlineService.get().detectSpeakers(audioData)
            }
        } finally {
            performanceMonitor.get().endOperation("combined_detect_speakers")
        }
    }

    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> {
        performanceMonitor.get().startOperation("combined_identify_speakers")
        return try {
            if (networkChecker.isNetworkAvailable()) {
                onlineService.get().identifySpeakers(audioData)
            } else {
                offlineService.get().identifySpeakers(audioData)
            }
        } finally {
            performanceMonitor.get().endOperation("combined_identify_speakers")
        }
    }

    override suspend fun generateSummary(text: String, template: SummaryTemplate?): Result<Summary> {
        performanceMonitor.get().startOperation("combined_generate_summary")
        return try {
            if (networkChecker.isNetworkAvailable()) {
                onlineService.get().generateSummary(text, template)
            } else {
                offlineService.get().generateSummary(text, template)
            }
        } finally {
            performanceMonitor.get().endOperation("combined_generate_summary")
        }
    }

    override suspend fun cleanup() {
        if (networkChecker.isNetworkAvailable()) {
            onlineService.get().cleanup()
        } else {
            offlineService.get().cleanup()
        }
    }

    override fun isInitialized(): Boolean {
        return if (networkChecker.isNetworkAvailable()) {
            onlineService.get().isInitialized()
        } else {
            offlineService.get().isInitialized()
        }
    }
}