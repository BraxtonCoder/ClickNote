package com.example.clicknote.domain.audio

interface AmplitudeProcessor {
    fun processAmplitude(buffer: ByteArray, readSize: Int): Int
    fun calculateRms(buffer: ByteArray, readSize: Int): Float
    fun getMaxAmplitude(): Int
    fun reset()
} 