package com.example.clicknote.service

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

@Singleton
class SpeakerDetectionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val minSilenceDuration = 0.5 // seconds
    private val energyThreshold = 0.1
    private val windowSize = 1024
    private val minSegmentDuration = 2.0 // seconds

    data class SpeakerSegment(
        val speakerId: Int,
        val startTime: Double,
        val endTime: Double,
        val confidence: Float
    )

    sealed class DetectionResult {
        data class Success(
            val segments: List<SpeakerSegment>,
            val speakerCount: Int
        ) : DetectionResult()
        data class Error(val message: String) : DetectionResult()
    }

    fun detectSpeakers(audioFile: File): Flow<DetectionResult> = flow {
        try {
            val audioData = loadAudioData(audioFile)
            val segments = segmentAudio(audioData)
            val speakerSegments = assignSpeakers(segments)
            
            emit(DetectionResult.Success(
                segments = speakerSegments,
                speakerCount = speakerSegments.map { it.speakerId }.distinct().size
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting speakers", e)
            emit(DetectionResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.Default)

    private fun loadAudioData(audioFile: File): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(audioFile.path)
        extractor.selectTrack(0)
        
        val format = extractor.getTrackFormat(0)
        val duration = format.getLong(MediaFormat.KEY_DURATION)
        val buffer = ByteBuffer.allocate(duration.toInt())
        
        var offset = 0
        while (extractor.readSampleData(buffer, offset) >= 0) {
            offset += extractor.readSampleData(buffer, offset)
            extractor.advance()
        }
        
        buffer.rewind()
        val floatArray = FloatArray(buffer.remaining() / 2)
        buffer.asShortBuffer().get(ShortArray(floatArray.size)).forEachIndexed { index, value ->
            floatArray[index] = value / 32768f
        }
        
        extractor.release()
        return floatArray
    }

    private fun segmentAudio(audioData: FloatArray): List<Pair<Int, Int>> {
        val segments = mutableListOf<Pair<Int, Int>>()
        var segmentStart = 0
        var inSilence = true
        var silenceStart = 0
        
        // Calculate energy for each window
        for (i in audioData.indices step windowSize) {
            val windowEnd = minOf(i + windowSize, audioData.size)
            val energy = calculateEnergy(audioData.slice(i until windowEnd))
            
            if (energy < energyThreshold) {
                if (!inSilence) {
                    silenceStart = i
                    inSilence = true
                }
            } else {
                if (inSilence) {
                    val silenceDuration = (i - silenceStart) / 16000.0 // assuming 16kHz sample rate
                    if (silenceDuration >= minSilenceDuration && segmentStart < silenceStart) {
                        segments.add(segmentStart to silenceStart)
                        segmentStart = i
                    }
                    inSilence = false
                }
            }
        }
        
        // Add final segment if needed
        if (segmentStart < audioData.size) {
            segments.add(segmentStart to audioData.size)
        }
        
        return segments
    }

    private fun calculateEnergy(window: List<Float>): Double {
        var sum = 0.0
        window.forEach { sample ->
            sum += sample * sample
        }
        return sqrt(sum / window.size)
    }

    private fun assignSpeakers(segments: List<Pair<Int, Int>>): List<SpeakerSegment> {
        val speakerSegments = mutableListOf<SpeakerSegment>()
        var currentSpeakerId = 1
        
        segments.forEach { (start, end) ->
            val duration = (end - start) / 16000.0 // assuming 16kHz sample rate
            if (duration >= minSegmentDuration) {
                speakerSegments.add(
                    SpeakerSegment(
                        speakerId = currentSpeakerId,
                        startTime = start / 16000.0,
                        endTime = end / 16000.0,
                        confidence = 0.8f // This would come from a real speaker recognition model
                    )
                )
                currentSpeakerId = if (currentSpeakerId == 1) 2 else 1
            }
        }
        
        return speakerSegments
    }

    companion object {
        private const val TAG = "SpeakerDetectionService"
    }
} 