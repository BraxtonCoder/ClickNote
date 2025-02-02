package com.example.clicknote.data.converter

import androidx.room.TypeConverter
import com.example.clicknote.domain.model.NoteSource
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Converters {
    private val SEPARATOR = "||"
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
    fun fromStringList(value: String?): List<String>? {
        return value?.split(SEPARATOR)?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(SEPARATOR)
    }
    
    @TypeConverter
    fun fromNoteSource(source: NoteSource): String {
        return source.name
    }

    @TypeConverter
    fun toNoteSource(value: String): NoteSource {
        return NoteSource.valueOf(value)
    }
} 