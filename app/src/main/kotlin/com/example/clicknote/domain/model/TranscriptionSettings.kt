package com.example.clicknote.domain.model

data class TranscriptionSettings(
    val language: String? = null,
    val model: String = "whisper-1",
    val prompt: String? = null,
    val temperature: Float = 0f,
    val speakerDetection: Boolean = false,
    val enhanceAudio: Boolean = false,
    val saveAudio: Boolean = true,
    val maxDuration: Long? = null,
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val bitsPerSample: Int = 16
)

enum class AudioQuality {
    LOW,    // 8kHz, mono
    MEDIUM, // 16kHz, mono
    HIGH    // 44.1kHz, stereo
} 