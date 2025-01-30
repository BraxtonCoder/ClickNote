package com.example.clicknote.data

import androidx.room.TypeConverter
import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }
    
    @TypeConverter
    fun fromColor(color: Color): Long {
        return color.value.toLong()
    }
    
    @TypeConverter
    fun toColor(value: Long): Color {
        return Color(value.toULong())
    }
} 