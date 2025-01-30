package com.example.clicknote.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitor @Inject constructor() {
    private val metrics = mutableMapOf<String, MetricStats>()
    private val thresholds = mutableMapOf<String, Long>()
    
    init {
        // Set default thresholds in milliseconds
        thresholds["amplitude_processing"] = 50L // 50ms
        thresholds["fft_processing"] = 20L // 20ms
        thresholds["audio_processing"] = 100L // 100ms
    }

    fun startMeasurement(metricName: String) {
        val stats = metrics.getOrPut(metricName) { MetricStats() }
        stats.startTime = System.nanoTime()
    }

    fun endMeasurement(metricName: String) {
        val stats = metrics[metricName] ?: return
        val endTime = System.nanoTime()
        val duration = (endTime - stats.startTime) / 1_000_000 // Convert to milliseconds
        
        stats.apply {
            totalTime += duration
            sampleCount++
            maxTime = maxOf(maxTime, duration)
            minTime = if (minTime == 0L) duration else minOf(minTime, duration)
            
            // Check if duration exceeds threshold
            thresholds[metricName]?.let { threshold ->
                if (duration > threshold) {
                    Log.w(TAG, "Performance warning: $metricName took ${duration}ms (threshold: ${threshold}ms)")
                }
            }
        }
    }

    fun setThreshold(metricName: String, thresholdMs: Long) {
        thresholds[metricName] = thresholdMs
    }

    fun logMetrics() {
        metrics.forEach { (name, stats) ->
            if (stats.sampleCount > 0) {
                val avgTime = stats.totalTime / stats.sampleCount
                Log.d(TAG, """
                    Performance metrics for $name:
                    Average time: ${avgTime}ms
                    Max time: ${stats.maxTime}ms
                    Min time: ${stats.minTime}ms
                    Sample count: ${stats.sampleCount}
                """.trimIndent())
            }
        }
    }

    fun reset() {
        metrics.clear()
    }

    private data class MetricStats(
        var startTime: Long = 0,
        var totalTime: Long = 0,
        var sampleCount: Long = 0,
        var maxTime: Long = 0,
        var minTime: Long = 0
    )

    companion object {
        private const val TAG = "PerformanceMonitor"
    }
}

interface PerformanceMonitor {
    fun startMonitoring(tag: String)
    fun stopMonitoring(tag: String)
    fun logMetric(tag: String, value: Long)
    fun getAverageMetric(tag: String): Long
    fun clearMetrics(tag: String)
} 