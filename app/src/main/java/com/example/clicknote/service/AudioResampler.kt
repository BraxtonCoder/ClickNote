package com.example.clicknote.service

import android.media.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioResampler @Inject constructor() {
    companion object {
        private const val TARGET_SAMPLE_RATE = 16000
        private const val TARGET_CHANNEL_COUNT = 1
        private const val BYTES_PER_SAMPLE = 2 // 16-bit audio
        
        fun resampleAudio(
            inputData: ByteArray,
            inputSampleRate: Int,
            inputChannelCount: Int
        ): ByteArray {
            if (inputSampleRate == TARGET_SAMPLE_RATE && inputChannelCount == TARGET_CHANNEL_COUNT) {
                return inputData
            }

            // Convert input bytes to shorts
            val inputBuffer = ByteBuffer.wrap(inputData).order(ByteOrder.LITTLE_ENDIAN)
            val inputShorts = ShortArray(inputData.size / 2)
            for (i in inputShorts.indices) {
                inputShorts[i] = inputBuffer.short
            }

            // Calculate output size
            val outputSampleCount = (inputShorts.size.toLong() * TARGET_SAMPLE_RATE / inputSampleRate).toInt()
            val outputShorts = ShortArray(outputSampleCount)

            // Create AudioTrack for resampling
            val bufferSize = AudioTrack.getMinBufferSize(
                TARGET_SAMPLE_RATE,
                TARGET_CHANNEL_COUNT,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(TARGET_SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            // Create AudioRecord for input
            val minRecordBufferSize = AudioRecord.getMinBufferSize(
                inputSampleRate,
                if (inputChannelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(inputSampleRate)
                    .setChannelMask(if (inputChannelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO)
                    .build())
                .setBufferSizeInBytes(minRecordBufferSize)
                .build()

            try {
                // Write input data
                audioTrack.write(inputShorts, 0, inputShorts.size)
                
                // Read resampled data
                var totalBytesRead = 0
                val tempBuffer = ShortArray(bufferSize / 2)
                
                while (totalBytesRead < outputShorts.size) {
                    val bytesRead = audioTrack.getPlaybackHeadPosition()
                    
                    if (bytesRead <= 0) break
                    
                    System.arraycopy(tempBuffer, 0, outputShorts, totalBytesRead, bytesRead)
                    totalBytesRead += bytesRead.toInt()
                }

                // Convert to mono if needed
                val monoOutput = if (inputChannelCount > 1) {
                    val monoSize = outputShorts.size / inputChannelCount
                    ShortArray(monoSize) { i ->
                        var sum = 0
                        for (ch in 0 until inputChannelCount) {
                            sum += outputShorts[i * inputChannelCount + ch]
                        }
                        (sum / inputChannelCount).toShort()
                    }
                } else {
                    outputShorts
                }

                // Convert shorts back to bytes
                val outputBuffer = ByteBuffer.allocate(monoOutput.size * 2).order(ByteOrder.LITTLE_ENDIAN)
                monoOutput.forEach { outputBuffer.putShort(it) }
                return outputBuffer.array()
            } finally {
                audioTrack.release()
                audioRecord.release()
            }
        }

        fun calculateRmsAmplitude(audioData: ByteArray): Float {
            val shorts = ShortArray(audioData.size / 2)
            ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            
            var sum = 0.0
            for (sample in shorts) {
                sum += (sample * sample).toDouble()
            }
            return kotlin.math.sqrt(sum / shorts.size).toFloat()
        }

        private fun resamplePCM16(inputData: ByteArray, inputSampleRate: Int, outputSampleRate: Int): ByteArray {
            val ratio = outputSampleRate.toDouble() / inputSampleRate
            val outputSize = (inputData.size * ratio).toInt()
            val outputData = ByteArray(outputSize)
            
            var inputIndex = 0
            var outputIndex = 0
            
            while (outputIndex < outputSize - 1) {
                val inputSample = (inputData[inputIndex].toInt() and 0xFF) or 
                                ((inputData[inputIndex + 1].toInt() and 0xFF) shl 8)
                
                outputData[outputIndex] = inputSample.toByte()
                outputData[outputIndex + 1] = (inputSample shr 8).toByte()
                
                inputIndex += (1 / ratio * 2).toInt()
                outputIndex += 2
            }
            
            return outputData
        }
    }

    fun resample(input: ShortArray, inputSampleRate: Int, outputSampleRate: Int): ShortArray {
        if (inputSampleRate == outputSampleRate) return input
        
        val ratio = inputSampleRate.toDouble() / outputSampleRate
        val outputLength = (input.size / ratio).toInt()
        val output = ShortArray(outputLength)
        
        for (i in output.indices) {
            val inputIndex = (i * ratio).toInt()
            output[i] = input[inputIndex.coerceIn(0, input.lastIndex)]
        }
        
        return output
    }
} 