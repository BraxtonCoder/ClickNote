package com.example.clicknote.data.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.clicknote.data.entity.NoteSource
import com.example.clicknote.data.entity.TranscriptionState
import com.example.clicknote.data.model.SyncStatus
import com.example.clicknote.data.entity.TranscriptionSegment
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

@ProvidedTypeConverter
class RoomConverters @Inject constructor() {
    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // DateTime conversions
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
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
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
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
    fun fromNoteSource(source: NoteSource): String {
        return source.name
    }

    @TypeConverter
    fun toNoteSource(value: String): NoteSource {
        return NoteSource.valueOf(value)
    }

    @TypeConverter
    fun fromTranscriptionState(state: TranscriptionState): String {
        return state.name
    }

    @TypeConverter
    fun toTranscriptionState(value: String): TranscriptionState {
        return TranscriptionState.valueOf(value)
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }

    // TranscriptionLanguage conversions
    @TypeConverter
    fun fromTranscriptionLanguage(language: TranscriptionLanguage): String {
        return language.code
    }

    @TypeConverter
    fun toTranscriptionLanguage(value: String): TranscriptionLanguage {
        return TranscriptionLanguage.fromCode(value)
    }

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

    @TypeConverter
    fun fromZonedDateTime(value: String?): ZonedDateTime? {
        return value?.let { ZonedDateTime.parse(it) }
    }

    @TypeConverter
    fun zonedDateTimeToString(date: ZonedDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromIntString(value: String?): List<Int>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun mapToString(map: Map<String, String>?): String? {
        if (map == null) return null
        return gson.toJson(map)
    }

    @TypeConverter
    fun stringToMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String> {
        if (value == null) return emptyMap()
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun toStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
} 