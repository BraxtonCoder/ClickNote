package com.example.clicknote.data.converter

import androidx.room.TypeConverter
import com.example.clicknote.data.entity.TranscriptionTimestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TranscriptionTimestampConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestampList(value: List<TranscriptionTimestamp>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTimestampList(value: String?): List<TranscriptionTimestamp>? {
        if (value == null) return null
        val listType = object : TypeToken<List<TranscriptionTimestamp>>() {}.type
        return gson.fromJson(value, listType)
    }
} 