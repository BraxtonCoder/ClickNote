package com.example.clicknote.data.converter

import androidx.room.TypeConverter
import com.example.clicknote.data.entity.NoteSource
import com.example.clicknote.data.entity.TranscriptionState
import com.example.clicknote.domain.model.TranscriptionSegment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class RoomConverters {
    private val gson = Gson()

    // DateTime conversions
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    // Date conversions
    @TypeConverter
    fun fromDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toDate(date: Date?): Long? {
        return date?.time
    }

    // Simple List conversions using Gson
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // Enum conversions
    @TypeConverter
    fun fromNoteSource(source: NoteSource): String = source.name

    @TypeConverter
    fun toNoteSource(value: String): NoteSource = NoteSource.valueOf(value)

    @TypeConverter
    fun fromTranscriptionState(state: TranscriptionState): String = state.name

    @TypeConverter
    fun toTranscriptionState(value: String): TranscriptionState = TranscriptionState.valueOf(value)

    // TranscriptionSegment conversions
    @TypeConverter
    fun fromTranscriptionSegments(segments: List<TranscriptionSegment>): String {
        return gson.toJson(segments)
    }

    @TypeConverter
    fun toTranscriptionSegments(value: String): List<TranscriptionSegment> {
        val type = object : TypeToken<List<TranscriptionSegment>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // FloatArray conversions
    @TypeConverter
    fun fromFloatArray(array: FloatArray?): String {
        return array?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        if (value.isEmpty()) return FloatArray(0)
        return value.split(",").map { it.toFloat() }.toFloatArray()
    }
} 