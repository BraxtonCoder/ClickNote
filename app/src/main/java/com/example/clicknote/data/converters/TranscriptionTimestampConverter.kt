package com.example.clicknote.data.converters

import androidx.room.TypeConverter
import com.example.clicknote.data.entity.TranscriptionTimestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TranscriptionTimestampConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<TranscriptionTimestamp>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<TranscriptionTimestamp> {
        val listType = object : TypeToken<List<TranscriptionTimestamp>>() {}.type
        return try {
            gson.fromJson(value, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
} 