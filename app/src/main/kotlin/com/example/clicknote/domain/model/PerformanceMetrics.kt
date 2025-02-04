package com.example.clicknote.domain.model

data class StorageMetrics(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val percentageUsed: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class NetworkMetrics(
    val bytesReceived: Long,
    val bytesSent: Long,
    val latency: Long,
    val connectionType: String,
    val isConnected: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class PerformanceReport(
    val timeRange: Long,
    val averageCpuUsage: Float,
    val peakCpuUsage: Float,
    val averageMemoryUsage: Long,
    val peakMemoryUsage: Long,
    val averageBatteryDrain: Float,
    val storageMetrics: StorageMetrics,
    val networkMetrics: NetworkMetrics,
    val events: Map<String, List<EventMetric>>,
    val measurements: Map<String, MeasurementMetric>,
    val operations: Map<String, OperationMetric>,
    val timestamp: Long = System.currentTimeMillis()
)

data class EventMetric(
    val name: String,
    val params: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis()
)

data class MeasurementMetric(
    val operationName: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long = endTime - startTime
)

data class OperationMetric(
    val name: String,
    val count: Int,
    val totalDuration: Long,
    val averageDuration: Long,
    val minDuration: Long,
    val maxDuration: Long,
    val lastExecuted: Long
) 