package com.example.clicknote.service.impl

import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.*
import android.util.Log
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.Speaker
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.service.PerformanceMonitor
import com.example.clicknote.domain.service.WhisperOfflineTranscriptionService
import com.example.clicknote.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class WhisperOfflineTranscriptionServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val performanceMonitor: Lazy<PerformanceMonitor>,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : WhisperOfflineTranscriptionService {
    
    private val _transcriptionProgress = MutableStateFlow(0f)
    override val transcriptionProgress: Flow<Float> = _transcriptionProgress.asStateFlow()

    private val _detectedSpeakers = MutableStateFlow<List<Speaker>>(emptyList())
    override val detectedSpeakers: Flow<List<Speaker>> = _detectedSpeakers.asStateFlow()

    private var isTranscribing = false
    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var acousticEchoCanceler: AcousticEchoCanceler? = null
    private var automaticGainControl: AutomaticGainControl? = null
    private var dynamicsProcessing: DynamicsProcessing? = null

    override suspend fun transcribe(audioFile: File, detectSpeakers: Boolean): String {
        return try {
            performanceMonitor.get().trackFileTranscription(audioFile)
            // TODO: Implement offline transcription using Whisper
            "Sample offline transcription"
        } catch (e: Exception) {
            throw e
        }
    }

    override fun startRealtimeTranscription(): Flow<TranscriptionSegment> = flow {
        try {
            isTranscribing = true
            initializeAudioRecord()
            
            audioRecord?.startRecording()
            
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            
            val buffer = ShortArray(bufferSize)
            
            while (isTranscribing) {
                val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (readSize > 0) {
                    // Process audio data and emit transcription segments
                    emit(TranscriptionSegment(
                        text = "Sample transcription",
                        startTime = 0L,
                        endTime = 0L,
                        confidence = 1.0f,
                        speaker = "Speaker 1"
                    ))
                }
            }
        } finally {
            cleanup()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun stopRealtimeTranscription() {
        isTranscribing = false
        cleanup()
    }

    private fun initializeAudioRecord() {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        // Initialize audio effects if available
        val audioSessionId = audioRecord?.audioSessionId ?: 0
        if (audioSessionId != 0) {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId)
            }
            if (AcousticEchoCanceler.isAvailable()) {
                acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)
            }
            if (AutomaticGainControl.isAvailable()) {
                automaticGainControl = AutomaticGainControl.create(audioSessionId)
            }
        }
    }

    private suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            audioRecord?.stop()
            audioRecord?.release()
            noiseSuppressor?.release()
            acousticEchoCanceler?.release()
            automaticGainControl?.release()
            dynamicsProcessing?.release()
            
            audioRecord = null
            noiseSuppressor = null
            acousticEchoCanceler = null
            automaticGainControl = null
            dynamicsProcessing = null
        }
    }

    private fun computeFFT(samples: DoubleArray): DoubleArray {
        val n = samples.size
        if (n == 0 || n and (n - 1) != 0) {
            throw IllegalArgumentException("Sample size must be a power of 2")
        }

        val real = samples.copyOf()
        val imag = DoubleArray(n)
        
        // Bit reversal
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                real[i] = real[j].also { real[j] = real[i] }
                imag[i] = imag[j].also { imag[j] = imag[i] }
            }
            var k = n shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }

        // FFT computation
        var stage = 1
        while (stage < n) {
            val wReal = cos(-PI / stage)
            val wImag = sin(-PI / stage)
            
            var m = 0
            while (m < n) {
                var tmpReal = 1.0
                var tmpImag = 0.0
                
                for (i in 0 until stage) {
                    val j = m + i + stage
                    val trReal = tmpReal * real[j] - tmpImag * imag[j]
                    val trImag = tmpReal * imag[j] + tmpImag * real[j]
                    
                    real[j] = real[m + i] - trReal
                    imag[j] = imag[m + i] - trImag
                    real[m + i] += trReal
                    imag[m + i] += trImag
                    
                    val tmpOldReal = tmpReal
                    tmpReal = tmpOldReal * wReal - tmpImag * wImag
                    tmpImag = tmpOldReal * wImag + tmpImag * wReal
                }
                m += stage shl 1
            }
            stage = stage shl 1
        }

        // Compute magnitude
        return DoubleArray(n) { i ->
            sqrt(real[i] * real[i] + imag[i] * imag[i])
        }
    }

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioRecord.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioRecord.ENCODING_PCM_16BIT
        private const val PI = kotlin.math.PI
    }
}