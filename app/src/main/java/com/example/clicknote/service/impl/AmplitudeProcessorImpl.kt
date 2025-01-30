package com.example.clicknote.service.impl

import android.util.Log
import com.example.clicknote.service.AmplitudeProcessor
import com.example.clicknote.service.PerformanceMonitor
import com.example.clicknote.domain.interfaces.AmplitudeCache
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Implementation of AmplitudeProcessor interface.
 * Handles audio amplitude processing, FFT analysis, and waveform generation.
 */
@Singleton
class AmplitudeProcessorImpl @Inject constructor(
    private val performanceMonitor: PerformanceMonitor,
    private val amplitudeCache: AmplitudeCache
) : AmplitudeProcessor {
    private var fft: FloatFFT_1D? = null
    private var windowSize = 2048 // Power of 2 for FFT
    private var smoothingFactor = 0.2f
    @Volatile private var lastAmplitude = 0f
    private var normalizationEnabled = true
    
    private val _amplitude = MutableStateFlow(0f)
    private val _waveform = MutableStateFlow(FloatArray(100))
    private val amplitudes = mutableListOf<Float>()
    private val maxSize = MAX_AMPLITUDES

    companion object {
        private const val TAG = "AmplitudeProcessor"
        private const val MAX_AMPLITUDES = 100
        private const val MIN_DB = -60f
        private const val MAX_DB = 0f
    }

    override fun processAudioData(data: ByteArray, size: Int): Flow<List<Float>> = flow {
        performanceMonitor.startMeasurement("amplitude_processing")
        
        try {
            // Convert byte array to float array
            val floatData = ByteArray(size).let { bytes ->
                System.arraycopy(data, 0, bytes, 0, size)
                FloatArray(size / 2) { i ->
                    val short = (bytes[i * 2 + 1].toInt() shl 8) or (bytes[i * 2].toInt() and 0xFF)
                    short.toFloat() / Short.MAX_VALUE
                }
            }

            // Initialize FFT if needed
            if (fft == null || floatData.size != windowSize) {
                windowSize = floatData.size
                fft = FloatFFT_1D(windowSize.toLong())
            }

            // Apply Hann window
            val windowedData = FloatArray(windowSize) { i ->
                floatData[i] * (0.5f - 0.5f * kotlin.math.cos(2 * Math.PI * i / (windowSize - 1))).toFloat()
            }

            // Perform FFT
            fft?.realForward(windowedData)

            // Calculate amplitude
            var maxAmplitude = 0f
            for (i in 0 until windowSize / 2) {
                val re = windowedData[2 * i]
                val im = windowedData[2 * i + 1]
                val magnitude = sqrt((re * re + im * im).toDouble()).toFloat()
                maxAmplitude = max(maxAmplitude, magnitude)
            }

            // Convert to dB and apply smoothing
            val dbValue = 20 * ln(maxAmplitude).toFloat()
            val normalizedDb = if (normalizationEnabled) {
                (dbValue - MIN_DB) / (MAX_DB - MIN_DB)
            } else {
                dbValue
            }
            val smoothedAmplitude = lastAmplitude * smoothingFactor + normalizedDb * (1 - smoothingFactor)
            lastAmplitude = smoothedAmplitude

            // Cache and emit new amplitudes
            amplitudeCache.cacheAmplitude(smoothedAmplitude)
            emit(amplitudeCache.get())

        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio data", e)
            emit(amplitudeCache.get())
        } finally {
            performanceMonitor.endMeasurement("amplitude_processing")
        }
    }

    override fun cleanup() {
        fft = null
        lastAmplitude = 0f
        amplitudeCache.clear()
        reset()
    }

    override fun release() {
        fft = null
        lastAmplitude = 0f
        performanceMonitor.logMetrics()
        cleanup()
    }

    override fun processAmplitude(amplitude: Float) {
        _amplitude.value = amplitude
        synchronized(amplitudes) {
            amplitudes.add(amplitude)
            if (amplitudes.size > maxSize) {
                amplitudes.removeAt(0)
            }
            _waveform.value = amplitudes.toFloatArray()
        }
        amplitudeCache.cacheAmplitude(amplitude)
    }

    override fun getAmplitudeFlow(): Flow<Float> = _amplitude.asStateFlow()

    override fun getWaveformFlow(): Flow<FloatArray> = _waveform.asStateFlow()

    override fun getProcessedAmplitudes(): Flow<List<Float>> = flow {
        emit(amplitudeCache.get())
    }

    override fun getWaveformData(): Flow<List<Float>> = flow {
        emit(amplitudes.toList())
    }

    override fun reset() {
        synchronized(amplitudes) {
            amplitudes.clear()
            _amplitude.value = 0f
            _waveform.value = FloatArray(maxSize) { 0f }
        }
        amplitudeCache.clear()
    }

    override fun getAverageAmplitude(): Float {
        return synchronized(amplitudes) {
            if (amplitudes.isEmpty()) 0f
            else amplitudes.average().toFloat()
        }
    }

    override fun getPeakAmplitude(): Float {
        return synchronized(amplitudes) {
            if (amplitudes.isEmpty()) 0f
            else amplitudes.maxOrNull() ?: 0f
        }
    }

    override fun setWindowSize(size: Int) {
        windowSize = size
        fft = null // Force FFT reinitialization with new size
    }

    override fun setSmoothingFactor(factor: Float) {
        smoothingFactor = factor.coerceIn(0f, 1f)
    }

    override fun setNormalizationEnabled(enabled: Boolean) {
        normalizationEnabled = enabled
    }
} 