package com.example.clicknote.data.converters

import androidx.room.TypeConverter
import com.example.clicknote.data.entity.NoteSource

class NoteSourceConverter {
    @TypeConverter
    fun fromNoteSource(source: NoteSource): String {
        return source.name
    }

    @TypeConverter
    fun toNoteSource(value: String): NoteSource {
        return NoteSource.valueOf(value)
    }
} 