package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface AudioPlayer {
    fun play(filePath: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun setPlaybackSpeed(speed: Float)
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun getPlaybackState(): Flow<PlaybackState>
    fun release()
}

enum class PlaybackState {
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR
} 