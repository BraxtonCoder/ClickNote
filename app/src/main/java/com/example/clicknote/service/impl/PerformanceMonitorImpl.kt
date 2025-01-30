package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.service.PerformanceMonitor
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

    private val monitoringTags = mutableMapOf<String, Long>()
    private val metrics = mutableMapOf<String, MutableMap<String, Double>>()
    private val events = mutableMapOf<String, MutableList<String>>()
    private val _metricsFlow = MutableStateFlow<Map<String, Map<String, Double>>>(emptyMap())
    private val eventsFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    override fun startMonitoring(tag: String) {
        monitoringTags[tag] = System.currentTimeMillis()
        if (!metrics.containsKey(tag)) {
            metrics[tag] = mutableMapOf()
            events[tag] = mutableListOf()
            updateFlows()
        }
    }

    override fun stopMonitoring(tag: String) {
        monitoringTags.remove(tag)?.let { startTime ->
            val duration = System.currentTimeMillis() - startTime
            logMetric(tag, "duration_ms", duration.toDouble())
        }
    }

    override fun trackFileTranscription(file: File) {
        startMonitoring("file_transcription")
        logMetric("file_transcription", "size_bytes", file.length().toDouble())
    }

    override fun trackAudioProcessing() {
        startMonitoring("audio_processing")
    }

    override fun trackError(error: Throwable) {
        logEvent("error", error.message ?: "Unknown error")
    }

    override fun logEvent(tag: String, message: String) {
        if (monitoringTags.contains(tag)) {
            events.getOrPut(tag) { mutableListOf() }.add(message)
            updateFlows()
        }
    }

    override fun logMetric(tag: String, metric: String, value: Double) {
        metrics.getOrPut(tag) { mutableMapOf() }[metric] = value
        _metricsFlow.value = metrics.toMap()
        updateFlows()
    }

    override fun getMetrics(tag: String): Flow<Map<String, Double>> {
        return _metricsFlow.asStateFlow()
    }

    override fun getEvents(tag: String): Flow<List<String>> {
        return eventsFlow.map { it[tag] ?: emptyList() }
    }

    override fun clearMetrics(tag: String) {
        metrics[tag]?.clear()
        events[tag]?.clear()
        updateFlows()
    }

    override fun cleanup() {
        metrics.clear()
        events.clear()
        monitoringTags.clear()
        updateFlows()
    }

    private fun updateFlows() {
        _metricsFlow.value = metrics.toMap()
        eventsFlow.value = events.toMap()
    }
} 