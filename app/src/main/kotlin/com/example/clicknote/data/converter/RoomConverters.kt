package com.example.clicknote.data.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.clicknote.data.entity.NoteSource
import com.example.clicknote.data.entity.TranscriptionState
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.data.entity.TranscriptionSegmentEntity
import com.example.clicknote.domain.model.SyncStatus
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
import javax.inject.Singleton

@Singleton
class RoomConverters @Inject constructor(
    private val gson: Gson
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // DateTime conversions
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)?.times(1000)
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
        return try {
            val listType = object : TypeToken<ArrayList<String>>() {}.type
            gson.fromJson<ArrayList<String>>(value, listType) ?: ArrayList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return gson.toJson(value ?: emptyList<Long>())
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return try {
            val listType = object : TypeToken<ArrayList<Long>>() {}.type
            gson.fromJson<ArrayList<Long>>(value, listType) ?: ArrayList()
        } catch (e: Exception) {
            emptyList()
        }
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

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    // TranscriptionLanguage conversions
    @TypeConverter
    fun fromTranscriptionLanguage(language: TranscriptionLanguage): String = language.code

    @TypeConverter
    fun toTranscriptionLanguage(value: String): TranscriptionLanguage = TranscriptionLanguage.fromCode(value)

    // TranscriptionSegment conversions
    @TypeConverter
    fun fromTranscriptionSegmentsJson(value: String?): List<TranscriptionSegment> {
        if (value == null) return emptyList()
        return try {
            val listType = object : TypeToken<ArrayList<TranscriptionSegmentEntity>>() {}.type
            val entities = gson.fromJson<ArrayList<TranscriptionSegmentEntity>>(value, listType) ?: ArrayList()
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toTranscriptionSegmentsJson(segments: List<TranscriptionSegment>?): String {
        if (segments == null) return "[]"
        val entities = segments.map { TranscriptionSegmentEntity.fromDomain("", it) }
        return gson.toJson(entities)
    }

    // Map conversions
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String> {
        if (value == null) return emptyMap()
        return try {
            val mapType = object : TypeToken<HashMap<String, String>>() {}.type
            gson.fromJson<HashMap<String, String>>(value, mapType) ?: HashMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun toStringMap(map: Map<String, String>?): String {
        return gson.toJson(map ?: emptyMap<String, String>())
    }

    @TypeConverter
    fun fromFloatMap(value: String?): Map<String, Float> {
        if (value == null) return emptyMap()
        return try {
            val mapType = object : TypeToken<HashMap<String, Float>>() {}.type
            gson.fromJson<HashMap<String, Float>>(value, mapType) ?: HashMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun toFloatMap(map: Map<String, Float>?): String {
        return gson.toJson(map ?: emptyMap<String, Float>())
    }
} 