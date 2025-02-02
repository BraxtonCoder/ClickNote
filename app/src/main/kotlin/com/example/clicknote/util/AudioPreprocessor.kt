package com.example.clicknote.util

import android.media.audiofx.NoiseSuppressor
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.DynamicsProcessing
import android.util.Log
import com.example.clicknote.analytics.AnalyticsTracker
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class AudioPreprocessor @Inject constructor(
    private val analyticsTracker: AnalyticsTracker
) {
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null
    private var dynamicsProcessor: DynamicsProcessing? = null

    companion object {
        private const val TAG = "AudioPreprocessor"
        private const val VAD_FRAME_SIZE = 480 // 30ms at 16kHz
        private const val VAD_THRESHOLD = 0.3f
        private const val NOISE_GATE_THRESHOLD = -60f // dB
        private const val MIN_SPEECH_DURATION = 0.3f // seconds
    }

    fun setupAudioEffects(audioSessionId: Int) {
        try {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply {
                    enabled = true
                }
            }

            if (AutomaticGainControl.isAvailable()) {
                gainControl = AutomaticGainControl.create(audioSessionId)?.apply {
                    enabled = true
                }
            }

            setupDynamicsProcessing(audioSessionId)
            
            analyticsTracker.trackPerformanceMetric(
                metricName = "audio_effects_setup",
                durationMs = 0,
                success = true,
                additionalData = mapOf(
                    "noise_suppressor" to (noiseSuppressor != null),
                    "gain_control" to (gainControl != null),
                    "dynamics_processor" to (dynamicsProcessor != null)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up audio effects", e)
            analyticsTracker.trackPerformanceMetric(
                metricName = "audio_effects_setup",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    private fun setupDynamicsProcessing(audioSessionId: Int) {
        if (DynamicsProcessing.isAvailable()) {
            val config = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                1, // channels
                true, // pre-eq
                true, // multi-band compressor
                true, // post-eq
                true, // limiter
                true  // noise gate
            ).build()

            dynamicsProcessor = DynamicsProcessing(0, audioSessionId, config).apply {
                enabled = true
                // Configure noise gate
                getConfig().getNoisegateInstance(0)?.let { noiseGate ->
                    noiseGate.setEnabled(true)
                    noiseGate.setThreshold(NOISE_GATE_THRESHOLD)
                }
            }
        }
    }

    fun detectVoiceActivity(audioData: FloatArray): List<VoiceSegment> {
        val startTime = System.currentTimeMillis()
        val segments = mutableListOf<VoiceSegment>()
        var currentSegment: VoiceSegment? = null
        
        // Process audio in frames
        for (i in audioData.indices step VAD_FRAME_SIZE) {
            val frameEnd = minOf(i + VAD_FRAME_SIZE, audioData.size)
            val frame = audioData.copyOfRange(i, frameEnd)
            val energy = calculateFrameEnergy(frame)
            val isSpeech = energy > VAD_THRESHOLD

            if (isSpeech && currentSegment == null) {
                currentSegment = VoiceSegment(
                    startIndex = i,
                    endIndex = frameEnd,
                    energy = energy
                )
            } else if (!isSpeech && currentSegment != null) {
                // Check if segment meets minimum duration
                val duration = (currentSegment.endIndex - currentSegment.startIndex) / 16000f
                if (duration >= MIN_SPEECH_DURATION) {
                    segments.add(currentSegment)
                }
                currentSegment = null
            } else if (isSpeech && currentSegment != null) {
                currentSegment.endIndex = frameEnd
                currentSegment.energy = maxOf(currentSegment.energy, energy)
            }
        }

        // Add final segment if it exists
        currentSegment?.let {
            val duration = (it.endIndex - it.startIndex) / 16000f
            if (duration >= MIN_SPEECH_DURATION) {
                segments.add(it)
            }
        }

        analyticsTracker.trackPerformanceMetric(
            metricName = "voice_activity_detection",
            durationMs = System.currentTimeMillis() - startTime,
            success = true,
            additionalData = mapOf(
                "segment_count" to segments.size,
                "audio_length" to audioData.size
            )
        )

        return segments
    }

    private fun calculateFrameEnergy(frame: FloatArray): Float {
        var energy = 0f
        for (sample in frame) {
            energy += sample * sample
        }
        return sqrt(energy / frame.size)
    }

    fun releaseAudioEffects() {
        try {
            noiseSuppressor?.release()
            gainControl?.release()
            dynamicsProcessor?.release()
            
            noiseSuppressor = null
            gainControl = null
            dynamicsProcessor = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio effects", e)
        }
    }

    data class VoiceSegment(
        val startIndex: Int,
        var endIndex: Int,
        var energy: Float
    )
} 