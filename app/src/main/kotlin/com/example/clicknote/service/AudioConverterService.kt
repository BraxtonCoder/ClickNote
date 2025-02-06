package com.example.clicknote.service

import android.content.Context
import android.media.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Singleton
class AudioConverterService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
        private const val BUFFER_SIZE = 4096
        private const val WAV_HEADER_SIZE = 44
        private const val AUDIO_FORMAT_PCM = 1 // PCM format code
    }

    sealed class AudioFormat(val extension: String) {
        object PCM : AudioFormat("pcm")
        object WAV : AudioFormat("wav")
        object MP3 : AudioFormat("mp3")
        object AAC : AudioFormat("aac")
        object M4A : AudioFormat("m4a")
        object OGG : AudioFormat("ogg")
        object FLAC : AudioFormat("flac")
        
        companion object {
            fun fromExtension(extension: String): AudioFormat {
                return when (extension.lowercase()) {
                    "pcm" -> PCM
                    "wav" -> WAV
                    "mp3" -> MP3
                    "aac" -> AAC
                    "m4a" -> M4A
                    "ogg" -> OGG
                    "flac" -> FLAC
                    else -> throw IllegalArgumentException("Unsupported format: $extension")
                }
            }
        }
    }

    suspend fun convertToWav(inputFile: File): File = withContext(Dispatchers.IO) {
        val format = AudioFormat.fromExtension(inputFile.extension)
        
        when (format) {
            is AudioFormat.PCM -> convertPcmToWav(inputFile)
            is AudioFormat.WAV -> inputFile
            is AudioFormat.MP3 -> convertMp3ToWav(inputFile)
            is AudioFormat.AAC -> convertAacToWav(inputFile)
            is AudioFormat.M4A -> convertM4aToWav(inputFile)
            is AudioFormat.OGG -> convertOggToWav(inputFile)
            is AudioFormat.FLAC -> convertFlacToWav(inputFile)
        }
    }

    fun convertPcmToWav(pcmFile: File): File {
        val wavFile = File(pcmFile.parent, pcmFile.nameWithoutExtension + ".wav")
        val pcmData = pcmFile.readBytes()
        
        DataOutputStream(BufferedOutputStream(FileOutputStream(wavFile))).use { output ->
            // Write WAV header
            writeWavHeader(output, pcmData.size)
            // Write PCM data
            output.write(pcmData)
        }
        
        return wavFile
    }

    private fun convertMp3ToWav(mp3File: File): File {
        val wavFile = File(mp3File.parent, mp3File.nameWithoutExtension + ".wav")
        val extractor = MediaExtractor()
        val muxer = MediaMuxer(wavFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        
        try {
            extractor.setDataSource(mp3File.path)
            val trackIndex = selectTrack(extractor)
            val format = extractor.getTrackFormat(trackIndex)
            
            // Configure decoder
            val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            decoder.configure(format, null, null, 0)
            
            // Process audio
            processAudioTrack(extractor, decoder, muxer, trackIndex)
            
            // Convert the muxed output to WAV
            convertToWavFormat(wavFile)
            
        } finally {
            extractor.release()
            muxer.release()
        }
        
        return wavFile
    }

    private fun convertAacToWav(aacFile: File): File {
        // Similar to MP3 conversion but with AAC-specific settings
        return convertGenericToWav(aacFile, MediaFormat.MIMETYPE_AUDIO_AAC)
    }

    private fun convertM4aToWav(m4aFile: File): File {
        // Similar to MP3 conversion but with M4A-specific settings
        return convertGenericToWav(m4aFile, MediaFormat.MIMETYPE_AUDIO_MPEG)
    }

    private fun convertOggToWav(oggFile: File): File {
        // Similar to MP3 conversion but with OGG-specific settings
        return convertGenericToWav(oggFile, MediaFormat.MIMETYPE_AUDIO_VORBIS)
    }

    private fun convertFlacToWav(flacFile: File): File {
        // Similar to MP3 conversion but with FLAC-specific settings
        return convertGenericToWav(flacFile, MediaFormat.MIMETYPE_AUDIO_FLAC)
    }

    private fun convertGenericToWav(inputFile: File, mimeType: String): File {
        val wavFile = File(inputFile.parent, inputFile.nameWithoutExtension + ".wav")
        val extractor = MediaExtractor()
        val muxer = MediaMuxer(wavFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        
        try {
            extractor.setDataSource(inputFile.path)
            val trackIndex = selectTrack(extractor)
            val format = extractor.getTrackFormat(trackIndex)
            
            // Configure decoder
            val decoder = MediaCodec.createDecoderByType(mimeType)
            decoder.configure(format, null, null, 0)
            
            // Process audio
            processAudioTrack(extractor, decoder, muxer, trackIndex)
            
            // Convert the muxed output to WAV
            convertToWavFormat(wavFile)
            
        } finally {
            extractor.release()
            muxer.release()
        }
        
        return wavFile
    }

    private fun selectTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                extractor.selectTrack(i)
                return i
            }
        }
        throw IllegalArgumentException("No audio track found")
    }

    private fun processAudioTrack(
        extractor: MediaExtractor,
        decoder: MediaCodec,
        muxer: MediaMuxer,
        trackIndex: Int
    ) {
        val bufferInfo = MediaCodec.BufferInfo()
        decoder.start()
        var outputTrackIndex = -1
        
        while (true) {
            val inputBufferIndex = decoder.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                
                if (sampleSize < 0) {
                    decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    break
                } else {
                    decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }
            
            val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)!!
                
                if (outputTrackIndex == -1) {
                    val format = decoder.getOutputFormat(outputBufferIndex)
                    outputTrackIndex = muxer.addTrack(format)
                    muxer.start()
                }
                
                muxer.writeSampleData(outputTrackIndex, outputBuffer, bufferInfo)
                decoder.releaseOutputBuffer(outputBufferIndex, false)
                
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
        
        decoder.stop()
        decoder.release()
    }

    private fun writeWavHeader(output: DataOutputStream, pcmDataSize: Int) {
        // RIFF header
        output.write("RIFF".toByteArray()) // ChunkID
        output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(36 + pcmDataSize).array()) // ChunkSize
        output.write("WAVE".toByteArray()) // Format
        
        // fmt subchunk
        output.write("fmt ".toByteArray()) // Subchunk1ID
        output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array()) // Subchunk1Size
        output.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array()) // AudioFormat (PCM = 1)
        output.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(CHANNELS.toShort()).array()) // NumChannels
        output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SAMPLE_RATE).array()) // SampleRate
        output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8).array()) // ByteRate
        output.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((CHANNELS * BITS_PER_SAMPLE / 8).toShort()).array()) // BlockAlign
        output.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(BITS_PER_SAMPLE.toShort()).array()) // BitsPerSample
        
        // data subchunk
        output.write("data".toByteArray()) // Subchunk2ID
        output.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(pcmDataSize).array()) // Subchunk2Size
    }

    private fun convertToWavFormat(file: File) {
        val tempFile = File(file.parent, "${file.nameWithoutExtension}_temp.wav")
        val pcmData = file.readBytes()
        
        DataOutputStream(BufferedOutputStream(FileOutputStream(tempFile))).use { output ->
            writeWavHeader(output, pcmData.size)
            output.write(pcmData)
        }
        
        file.delete()
        tempFile.renameTo(file)
    }

    fun splitWavFile(wavFile: File, chunkDurationMs: Int): List<File> {
        val bytesPerSample = BITS_PER_SAMPLE / 8
        val bytesPerSecond = SAMPLE_RATE * CHANNELS * bytesPerSample
        val bytesPerChunk = bytesPerSecond * chunkDurationMs / 1000
        
        val chunks = mutableListOf<File>()
        val inputData = wavFile.readBytes()
        val dataOffset = 44 // WAV header size
        
        var chunkIndex = 0
        var offset = dataOffset
        
        while (offset < inputData.size) {
            val remainingBytes = inputData.size - offset
            val chunkSize = minOf(bytesPerChunk, remainingBytes)
            
            val chunkFile = File(wavFile.parent, "${wavFile.nameWithoutExtension}_chunk$chunkIndex.wav")
            DataOutputStream(BufferedOutputStream(FileOutputStream(chunkFile))).use { output ->
                // Write WAV header for chunk
                writeWavHeader(output, chunkSize)
                // Write chunk data
                output.write(inputData, offset, chunkSize)
            }
            
            chunks.add(chunkFile)
            offset += chunkSize
            chunkIndex++
        }
        
        return chunks
    }

    fun cleanupTempFiles(files: List<File>) {
        files.forEach { file ->
            if (file.exists() && (file.name.contains("_chunk") || file.name.contains("enhanced_"))) {
                file.delete()
            }
        }
    }

    suspend fun convertToWav(inputFile: File, outputFile: File): Result<File> = runCatching {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputFile.absolutePath)

        val audioTrackIndex = selectAudioTrack(extractor)
        if (audioTrackIndex < 0) {
            throw IllegalStateException("No audio track found")
        }

        val format = extractor.getTrackFormat(audioTrackIndex)
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val duration = format.getLong(MediaFormat.KEY_DURATION)

        val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        decoder.configure(format, null, null, 0)
        decoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val outputFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_RAW,
            sampleRate,
            channelCount
        )
        val outputTrackIndex = muxer.addTrack(outputFormat)
        muxer.start()

        try {
            decodeAndMuxAudio(extractor, decoder, muxer, outputTrackIndex, audioTrackIndex)
        } finally {
            extractor.release()
            decoder.stop()
            decoder.release()
            muxer.stop()
            muxer.release()
        }

        // Add WAV header
        addWavHeader(outputFile, sampleRate, channelCount, duration)

        outputFile
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                extractor.selectTrack(i)
                return i
            }
        }
        return -1
    }

    private fun decodeAndMuxAudio(
        extractor: MediaExtractor,
        decoder: MediaCodec,
        muxer: MediaMuxer,
        outputTrackIndex: Int,
        audioTrackIndex: Int
    ) {
        val bufferInfo = MediaCodec.BufferInfo()
        val inputBuffers = decoder.inputBuffers
        val outputBuffers = decoder.outputBuffers
        var sawInputEOS = false
        var sawOutputEOS = false

        while (!sawOutputEOS) {
            if (!sawInputEOS) {
                val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = inputBuffers[inputBufferIndex]
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)

                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        sawInputEOS = true
                    } else {
                        decoder.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            sampleSize,
                            extractor.sampleTime,
                            0
                        )
                        extractor.advance()
                    }
                }
            }

            val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex >= 0) {
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    sawOutputEOS = true
                }

                if (bufferInfo.size > 0) {
                    val outputBuffer = outputBuffers[outputBufferIndex]
                    muxer.writeSampleData(outputTrackIndex, outputBuffer, bufferInfo)
                }
                decoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    private fun addWavHeader(outputFile: File, sampleRate: Int, channelCount: Int, duration: Long) {
        val pcmData = outputFile.readBytes()
        val wavFile = FileOutputStream(outputFile)

        val totalDataLen = pcmData.size + WAV_HEADER_SIZE - 8
        val byteRate = sampleRate * channelCount * BITS_PER_SAMPLE / 8

        val header = ByteBuffer.allocate(WAV_HEADER_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            // RIFF header
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())

            // fmt chunk
            put("fmt ".toByteArray())
            putInt(16) // fmt chunk size
            putShort(AUDIO_FORMAT_PCM.toShort()) // audio format (PCM)
            putShort(channelCount.toShort()) // channels
            putInt(sampleRate) // sample rate
            putInt(byteRate) // byte rate
            putShort((channelCount * BITS_PER_SAMPLE / 8).toShort()) // block align
            putShort(BITS_PER_SAMPLE.toShort()) // bits per sample

            // data chunk
            put("data".toByteArray())
            putInt(pcmData.size)
        }.array()

        wavFile.write(header)
        wavFile.write(pcmData)
        wavFile.close()
    }

    private fun ByteBuffer.reverseBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        get(bytes)
        return bytes.reversedArray()
    }
} 