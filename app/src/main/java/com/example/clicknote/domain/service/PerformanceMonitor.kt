package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface PerformanceMonitor {
    fun startMonitoring()
    fun stopMonitoring()
    fun isMonitoring(): Boolean
    fun getCpuUsage(): Flow<Float>
    fun getMemoryUsage(): Flow<Long>
    fun getBatteryUsage(): Flow<Float>
    fun getStorageMetrics(): Flow<StorageMetrics>
    fun getNetworkMetrics(): Flow<NetworkMetrics>
    fun logEvent(event: String, duration: Long)
    fun getPerformanceReport(): PerformanceReport
    suspend fun trackAudioProcessing()
    suspend fun trackFileTranscription(file: File)
    fun startMeasurement(name: String)
    fun endMeasurement(name: String)
}

data class StorageMetrics(
    val usedSpace: Long,
    val freeSpace: Long,
    val ioOperations: Int
)

data class NetworkMetrics(
    val bytesUploaded: Long,
    val bytesDownloaded: Long,
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