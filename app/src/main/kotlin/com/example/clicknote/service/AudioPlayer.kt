package com.example.clicknote.service

import kotlinx.coroutines.flow.Flow

interface AudioPlayer {
    val isPlaying: Flow<Boolean>
    val progress: Flow<Float>
    val duration: Flow<Long>

    suspend fun loadAudio(path: String)
    suspend fun play()
    suspend fun pause()
    suspend fun seekTo(progress: Float)
    suspend fun release()
} 