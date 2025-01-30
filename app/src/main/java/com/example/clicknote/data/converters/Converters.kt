package com.example.clicknote.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromString(value: String?): Map<String, String> {
        return value?.let { gson.fromJson(it, mapType) } ?: emptyMap()
    }

    @TypeConverter
    fun toString(map: Map<String, String>): String {
        return gson.toJson(map)
    }
} 