package com.example.clicknote.data.converters

import androidx.room.TypeConverter
import com.example.clicknote.data.entity.TranscriptionState

class TranscriptionStateConverter {
    @TypeConverter
    fun fromTranscriptionState(state: TranscriptionState): String {
        return state.name
    }

    @TypeConverter
    fun toTranscriptionState(value: String): TranscriptionState {
        return TranscriptionState.valueOf(value)
    }
} 