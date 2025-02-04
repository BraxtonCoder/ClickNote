package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.NetworkMetrics
import com.example.clicknote.domain.model.PerformanceReport
import com.example.clicknote.domain.model.StorageMetrics
import kotlinx.coroutines.flow.Flow

interface PerformanceMonitor {
    suspend fun startMonitoring()
    suspend fun stopMonitoring()
    fun isMonitoring(): Boolean
    
    fun getCpuUsage(): Flow<Float>
    fun getMemoryUsage(): Flow<Long>
    fun getBatteryUsage(): Flow<Float>
    fun getStorageMetrics(): Flow<StorageMetrics>
    fun getNetworkMetrics(): Flow<NetworkMetrics>
    
    suspend fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    suspend fun getPerformanceReport(timeRange: Long? = null): PerformanceReport
    
    suspend fun startMeasurement(operationName: String)
    suspend fun endMeasurement(operationName: String)
    
    suspend fun startOperation(name: String)
    suspend fun endOperation(name: String)
} 