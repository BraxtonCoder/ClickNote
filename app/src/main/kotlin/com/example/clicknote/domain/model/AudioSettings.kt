package com.example.clicknote.domain.model

import androidx.compose.ui.graphics.Color

data class AudioSettings(
    val skipDuration: Int = 10, // in seconds
    val waveformStyle: WaveformStyle = WaveformStyle.Bar,
    val waveformColors: WaveformColors = WaveformColors.Default,
    val exportFormat: AudioFormat = AudioFormat.M4A,
    val saveAudioWithNote: Boolean = true,
    val enhanceAudioQuality: Boolean = true
)

enum class AudioFormat(
    val extension: String,
    val mimeType: String,
    val displayName: String
) {
    M4A("m4a", "audio/mp4", "M4A (High Quality)"),
    MP3("mp3", "audio/mpeg", "MP3 (Compressed)"),
    WAV("wav", "audio/wav", "WAV (Lossless)"),
    AAC("aac", "audio/aac", "AAC (Standard)"),
    OGG("ogg", "audio/ogg", "OGG (Open Format)")
}

data class WaveformColors(
    val playedColor: Color,
    val unplayedColor: Color,
    val positionLineColor: Color
) {
    companion object {
        val Default = WaveformColors(
            playedColor = Color(0xFF2196F3),
            unplayedColor = Color(0xFFBBDEFB),
            positionLineColor = Color(0xFF2196F3)
        )
        
        val Dark = WaveformColors(
            playedColor = Color(0xFF82B1FF),
            unplayedColor = Color(0xFF304FFE),
            positionLineColor = Color(0xFF82B1FF)
        )
        
        val Minimal = WaveformColors(
            playedColor = Color(0xFF000000),
            unplayedColor = Color(0xFFAAAAAA),
            positionLineColor = Color(0xFF000000)
        )
        
        val Vibrant = WaveformColors(
            playedColor = Color(0xFFFF4081),
            unplayedColor = Color(0xFFFF80AB),
            positionLineColor = Color(0xFFFF4081)
        )
    }
}

enum class WaveformStyle {
    Bar,
    Line,
    Mirror
} 