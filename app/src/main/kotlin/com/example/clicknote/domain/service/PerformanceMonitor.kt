package com.example.clicknote.domain.service

import java.io.File

interface PerformanceMonitor {
    suspend fun trackAudioProcessing()
    suspend fun trackFileTranscription(file: File)
    suspend fun trackSpeakerDetection()
    suspend fun trackLanguageDetection()
    suspend fun trackSummarization()
    suspend fun trackCloudSync()
    suspend fun trackDatabaseOperation(operation: String)
    suspend fun trackNetworkRequest(endpoint: String)
    suspend fun trackMemoryUsage()
    suspend fun trackBatteryUsage()
    suspend fun trackCPUUsage()
    suspend fun trackStorageUsage()
    suspend fun trackUserInteraction(action: String)
    suspend fun trackError(error: Throwable)
    suspend fun trackLatency(operation: String, durationMs: Long)
    suspend fun flush()
}

data class StorageMetrics(
    val totalSpace: Long,
    val usedSpace: Long,
    val percentageUsed: Int
)

data class NetworkMetrics(
    val bytesReceived: Long,
    val bytesSent: Long,
    val latency: Long
)

data class PerformanceReport(
    val averageCpuUsage: Float,
    val peakMemoryUsage: Long,
    val averageBatteryDrain: Float,
    val storageMetrics: StorageMetrics,
    val networkMetrics: NetworkMetrics,
    val events: Map<String, List<Long>>
) 