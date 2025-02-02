package com.example.clicknote.data.util

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? {
        return value?.let { String(it) }
    }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        return value?.toByteArray()
    }
} 