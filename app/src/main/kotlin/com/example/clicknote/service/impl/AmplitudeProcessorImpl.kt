package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.service.AmplitudeProcessor
import com.example.clicknote.domain.service.PerformanceMonitor
import com.example.clicknote.domain.interfaces.AmplitudeCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Implementation of AmplitudeProcessor interface.
 * Handles audio amplitude processing, FFT analysis, and waveform generation.
 */
@Singleton
class AmplitudeProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceMonitor: Lazy<PerformanceMonitor>,
    private val amplitudeCache: Lazy<AmplitudeCache>
) : AmplitudeProcessor {
    private var windowSize = 2048 // Power of 2 for FFT
    private var smoothingFactor = 0.2f
    @Volatile private var lastAmplitude = 0f
    private var normalizationEnabled = true
    
    private val waveformBuffer = mutableListOf<Float>()
    private val maxBufferSize = 100

    override fun processAmplitude(amplitude: Float) {
        performanceMonitor.get().startMeasurement("amplitude_processing")
        try {
            val normalizedAmplitude = normalizeAmplitude(amplitude)
            waveformBuffer.add(normalizedAmplitude)
            
            if (waveformBuffer.size > maxBufferSize) {
                waveformBuffer.removeAt(0)
            }
            
            amplitudeCache.get().cacheAmplitude(normalizedAmplitude)
        } finally {
            performanceMonitor.get().endMeasurement("amplitude_processing")
        }
    }

    override fun getProcessedAmplitudes(): Flow<List<Float>> = flow {
        emit(amplitudeCache.get().get())
    }

    override fun getWaveformData(): Flow<List<Float>> = flow {
        emit(waveformBuffer.toList())
    }

    override fun getAverageAmplitude(): Float {
        return waveformBuffer.average().toFloat()
    }

    override fun getPeakAmplitude(): Float {
        return waveformBuffer.maxOrNull() ?: 0f
    }

    override fun reset() {
        waveformBuffer.clear()
        lastAmplitude = 0f
        amplitudeCache.get().clear()
    }

    override fun setWindowSize(size: Int) {
        windowSize = size
    }

    override fun setSmoothingFactor(factor: Float) {
        smoothingFactor = factor.coerceIn(0f, 1f)
    }

    override fun setNormalizationEnabled(enabled: Boolean) {
        normalizationEnabled = enabled
    }

    private fun normalizeAmplitude(amplitude: Float): Float {
        // Apply smoothing
        val smoothedAmplitude = lastAmplitude * smoothingFactor + amplitude * (1 - smoothingFactor)
        lastAmplitude = smoothedAmplitude

        // Convert to dB and normalize
        val dbValue = 20 * ln(smoothedAmplitude + 1e-10).toFloat()
        return if (normalizationEnabled) {
            (dbValue + 60) / 60 // Normalize to [0,1] assuming -60dB to 0dB range
        } else {
            dbValue
        }
    }
} 