package com.example.clicknote.domain.interfaces

interface AmplitudeCache {
    fun cacheAmplitude(amplitude: Float)
    fun get(): List<Float>
    fun getLastAmplitude(): Float
    fun clear()
    fun put(values: List<Float>)
} 