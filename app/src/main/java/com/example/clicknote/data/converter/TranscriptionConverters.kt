package com.example.clicknote.data.converter

import androidx.room.TypeConverter
import com.example.clicknote.domain.model.TranscriptionSegment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TranscriptionConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTranscriptionSegments(segments: List<TranscriptionSegment>): String {
        return gson.toJson(segments)
    }
    
    @TypeConverter
    fun toTranscriptionSegments(json: String): List<TranscriptionSegment> {
        val type = object : TypeToken<List<TranscriptionSegment>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
} 