package com.example.clicknote.data.service

import android.content.Context
import android.app.ActivityManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.net.TrafficStats
import com.example.clicknote.domain.service.PerformanceMonitor
import com.example.clicknote.domain.service.StorageMetrics
import com.example.clicknote.domain.service.NetworkMetrics
import com.example.clicknote.domain.service.PerformanceReport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PerformanceMonitor {
    private var isMonitoringActive = false
    private val cpuUsageFlow = MutableStateFlow(0f)
    private val memoryUsage = MutableStateFlow(0L)
    private val batteryUsage = MutableStateFlow(0f)
    private val storageMetrics = MutableStateFlow(StorageMetrics(0L, 0L, 0))
    private val networkMetrics = MutableStateFlow(NetworkMetrics(0L, 0L, 0L))
    private val events = mutableMapOf<String, MutableList<Long>>()
    private val measurements = mutableMapOf<String, Long>()
    private val operations = mutableMapOf<String, Long>()

    override fun startMonitoring() {
        isMonitoringActive = true
    }

    override fun stopMonitoring() {
        isMonitoringActive = false
        cpuUsageFlow.value = 0f
    }

    override fun isMonitoring(): Boolean = isMonitoringActive

    override fun getCpuUsage(): Flow<Float> = cpuUsageFlow.asStateFlow()

    override fun getMemoryUsage(): Flow<Long> = memoryUsage.asStateFlow()

    override fun getBatteryUsage(): Flow<Float> = batteryUsage.asStateFlow()

    override fun getStorageMetrics(): Flow<StorageMetrics> = storageMetrics.asStateFlow()

    override fun getNetworkMetrics(): Flow<NetworkMetrics> = networkMetrics.asStateFlow()

    override fun logEvent(event: String, duration: Long) {
        events.getOrPut(event) { mutableListOf() }.add(duration)
    }

    override fun getPerformanceReport(): PerformanceReport {
        return PerformanceReport(
            averageCpuUsage = cpuUsageFlow.value,
            peakMemoryUsage = memoryUsage.value,
            averageBatteryDrain = batteryUsage.value,
            storageMetrics = storageMetrics.value,
            networkMetrics = networkMetrics.value,
            events = events.toMap()
        )
    }

    override suspend fun trackAudioProcessing() {
        logEvent("audio_processing", System.currentTimeMillis())
    }

    override suspend fun trackFileTranscription(file: File) {
        logEvent("file_transcription", System.currentTimeMillis())
    }

    override fun startMeasurement(name: String) {
        measurements[name] = System.currentTimeMillis()
    }

    override fun endMeasurement(name: String) {
        val startTime = measurements.remove(name)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            logEvent(name, duration)
        }
    }

    override fun startOperation(operationName: String) {
        operations[operationName] = System.currentTimeMillis()
    }

    override fun endOperation(operationName: String) {
        val startTime = operations.remove(operationName)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            logEvent(operationName, duration)
        }
    }
} 