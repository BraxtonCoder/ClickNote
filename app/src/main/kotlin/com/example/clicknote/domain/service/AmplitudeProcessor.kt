package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

/**
 * Domain layer interface for processing audio amplitudes.
 * Defines core functionality for amplitude processing.
 */
interface AmplitudeProcessor {
    /**
     * Process a single amplitude value.
     */
    fun processAmplitude(amplitude: Float)
    
    /**
     * Get a flow of processed amplitude values.
     */
    fun getProcessedAmplitudes(): Flow<List<Float>>
    
    /**
     * Get a flow of waveform data.
     */
    fun getWaveformData(): Flow<List<Float>>
    
    /**
     * Get the average amplitude value.
     */
    fun getAverageAmplitude(): Float
    
    /**
     * Get the peak amplitude value.
     */
    fun getPeakAmplitude(): Float
    
    /**
     * Reset the processor state.
     */
    fun reset()
    
    /**
     * Set the window size for FFT processing.
     */
    fun setWindowSize(size: Int)
    
    /**
     * Set the smoothing factor for amplitude values.
     */
    fun setSmoothingFactor(factor: Float)
    
    /**
     * Enable or disable amplitude normalization.
     */
    fun setNormalizationEnabled(enabled: Boolean)
}

/**
 * Data class representing amplitude statistics.
 */
data class AmplitudeStats(
    val average: Float,
    val peak: Float,
    val rms: Float,
    val zeroCrossings: Int
) 