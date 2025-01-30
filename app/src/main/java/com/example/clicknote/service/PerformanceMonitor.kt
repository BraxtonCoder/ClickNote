package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface PerformanceMonitor {
    fun startMonitoring(tag: String)
    fun stopMonitoring(tag: String)
    fun trackFileTranscription(file: File)
    fun trackAudioProcessing()
    fun trackError(error: Throwable)
    fun logEvent(tag: String, message: String)
    fun logMetric(tag: String, metric: String, value: Double)
    fun getMetrics(tag: String): Flow<Map<String, Double>>
    fun getEvents(tag: String): Flow<List<String>>
    fun clearMetrics(tag: String)
    fun cleanup()
} 