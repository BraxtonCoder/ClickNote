package com.example.clicknote.domain.model

data class StorageMetrics(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val percentageUsed: Float
)

data class NetworkMetrics(
    val bytesReceived: Long,
    val bytesSent: Long,
    val latency: Long,
    val isConnected: Boolean,
    val networkType: String
)

data class PerformanceReport(
    val cpuUsage: Float,
    val memoryUsage: Long,
    val batteryUsage: Float,
    val storage: StorageMetrics,
    val network: NetworkMetrics,
    val events: List<PerformanceEvent>
)

data class PerformanceEvent(
    val name: String,
    val duration: Long,
    val timestamp: Long
) 