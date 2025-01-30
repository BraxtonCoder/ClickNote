package com.example.clicknote.data

enum class AudioQuality(
    val sampleRate: Int,
    val bitRate: Int,
    val channels: Int
) {
    LOW(16000, 32000, 1),      // 16kHz, 32kbps, Mono
    MEDIUM(44100, 128000, 1),  // 44.1kHz, 128kbps, Mono
    HIGH(44100, 192000, 2);    // 44.1kHz, 192kbps, Stereo

    companion object {
        fun fromString(value: String): AudioQuality {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                MEDIUM // Default to medium quality
            }
        }
    }
} 