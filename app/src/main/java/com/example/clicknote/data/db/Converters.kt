package com.example.clicknote.data.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.example.clicknote.domain.model.NoteSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromNoteSource(value: NoteSource): String {
        return value.name
    }

    @TypeConverter
    fun toNoteSource(value: String): NoteSource {
        return NoteSource.valueOf(value)
    }
} 