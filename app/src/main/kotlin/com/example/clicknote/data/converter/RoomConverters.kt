package com.example.clicknote.data.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.model.SyncStatus
import com.example.clicknote.domain.model.TranscriptionState
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.data.entity.TranscriptionSegmentEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedTypeConverter
class RoomConverters @Inject constructor(
    private val gson: Gson
) {
    @TypeConverter
    fun mapToString(map: Map<String, String>?): String {
        return try {
            gson.toJson(map ?: emptyMap<String, String>())
        } catch (e: Exception) {
            gson.toJson(emptyMap<String, String>())
        }
    }

    @TypeConverter
    fun stringToMap(value: String?): Map<String, String> {
        if (value == null) return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(value, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return try {
            gson.toJson(list ?: emptyList<String>())
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun fromNoteSource(source: NoteSource?): String {
        return source?.name ?: NoteSource.MANUAL.name
    }

    @TypeConverter
    fun toNoteSource(value: String?): NoteSource {
        return try {
            if (value == null) NoteSource.MANUAL
            else NoteSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            NoteSource.MANUAL
        }
    }

    @TypeConverter
    fun fromTranscriptionState(state: TranscriptionState?): String {
        return state?.name ?: TranscriptionState.PENDING.name
    }

    @TypeConverter
    fun toTranscriptionState(value: String?): TranscriptionState {
        return try {
            if (value == null) TranscriptionState.PENDING
            else TranscriptionState.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TranscriptionState.PENDING
        }
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String {
        return status?.name ?: SyncStatus.PENDING.name
    }

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus {
        return try {
            if (value == null) SyncStatus.PENDING
            else SyncStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncStatus.PENDING
        }
    }

    @TypeConverter
    fun fromTranscriptionLanguage(language: TranscriptionLanguage?): String {
        return language?.code ?: DEFAULT_LANGUAGE
    }

    @TypeConverter
    fun toTranscriptionLanguage(value: String?): TranscriptionLanguage {
        return try {
            if (value == null) TranscriptionLanguage.DEFAULT
            else TranscriptionLanguage.fromCode(value)
        } catch (e: IllegalArgumentException) {
            TranscriptionLanguage.DEFAULT
        }
    }

    @TypeConverter
    fun fromTranscriptionSegments(segments: List<TranscriptionSegment>?): String {
        if (segments == null) return "[]"
        return try {
            gson.toJson(segments)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toTranscriptionSegments(value: String?): List<TranscriptionSegment> {
        if (value == null) return emptyList()
        return try {
            val type = object : TypeToken<List<TranscriptionSegment>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        const val DEFAULT_LANGUAGE = "en"
    }
} 