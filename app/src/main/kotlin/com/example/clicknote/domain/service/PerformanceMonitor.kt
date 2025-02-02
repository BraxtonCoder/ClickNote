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
    fun startOperation(operationName: String)
    fun endOperation(operationName: String)
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