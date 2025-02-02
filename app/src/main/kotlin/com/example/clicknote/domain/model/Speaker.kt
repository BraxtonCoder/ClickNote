package com.example.clicknote.domain.model

data class Speaker(
    val id: String,
    val name: String,
    val confidence: Float,
    val startTime: Long,
    val endTime: Long
) 