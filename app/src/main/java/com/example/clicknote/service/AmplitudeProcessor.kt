package com.example.clicknote.service

import kotlinx.coroutines.flow.Flow

/**
 * Service layer interface for processing audio amplitudes.
 * Provides functionality for real-time audio processing and waveform visualization.
 */
interface AmplitudeProcessor {
    /**
     * Process raw audio data and return a flow of processed amplitude values.
     * @param data Raw audio data as byte array
     * @param size Size of the data to process
     * @return Flow of processed amplitude values
     */
    fun processAudioData(data: ByteArray, size: Int): Flow<List<Float>>
    
    /**
     * Get a flow of real-time amplitude values.
     * @return Flow of individual amplitude values
     */
    fun getAmplitudeFlow(): Flow<Float>
    
    /**
     * Get a flow of waveform data as a float array.
     * @return Flow of waveform data
     */
    fun getWaveformFlow(): Flow<FloatArray>
    
    /**
     * Process a single amplitude value.
     * @param amplitude Raw amplitude value to process
     */
    fun processAmplitude(amplitude: Float)
    
    /**
     * Get a flow of processed amplitude values.
     * @return Flow of processed amplitude values
     */
    fun getProcessedAmplitudes(): Flow<List<Float>>
    
    /**
     * Get a flow of waveform data.
     * @return Flow of waveform data points
     */
    fun getWaveformData(): Flow<List<Float>>
    
    /**
     * Get the average amplitude value.
     * @return Average amplitude as float
     */
    fun getAverageAmplitude(): Float
    
    /**
     * Get the peak amplitude value.
     * @return Peak amplitude as float
     */
    fun getPeakAmplitude(): Float
    
    /**
     * Reset the processor state.
     */
    fun reset()
    
    /**
     * Set the window size for FFT processing.
     * @param size Window size for FFT
     */
    fun setWindowSize(size: Int)
    
    /**
     * Set the smoothing factor for amplitude values.
     * @param factor Smoothing factor between 0.0 and 1.0
     */
    fun setSmoothingFactor(factor: Float)
    
    /**
     * Enable or disable amplitude normalization.
     * @param enabled Whether normalization should be enabled
     */
    fun setNormalizationEnabled(enabled: Boolean)
    
    /**
     * Clean up resources and reset state.
     */
    fun cleanup()
    
    /**
     * Release resources and perform final cleanup.
     */
    fun release()
} 