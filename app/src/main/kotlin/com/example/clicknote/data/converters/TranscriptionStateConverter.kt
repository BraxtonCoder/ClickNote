package com.example.clicknote.data.converters

import androidx.room.TypeConverter
import com.example.clicknote.data.entity.TranscriptionState as EntityState
import com.example.clicknote.domain.model.TranscriptionState as DomainState

class TranscriptionStateConverter {
    @TypeConverter
    fun fromTranscriptionState(state: DomainState): String {
        return when (state) {
            is DomainState.Idle -> EntityState.IDLE.name
            is DomainState.Recording -> EntityState.RECORDING.name
            is DomainState.Paused -> EntityState.PAUSED.name
            is DomainState.Processing -> EntityState.PROCESSING.name
            is DomainState.Completed -> EntityState.COMPLETED.name
            is DomainState.Error -> EntityState.ERROR.name
            is DomainState.Cancelled -> EntityState.CANCELLED.name
        }
    }

    @TypeConverter
    fun toTranscriptionState(value: String): DomainState {
        return when (EntityState.valueOf(value)) {
            EntityState.IDLE -> DomainState.Idle
            EntityState.RECORDING -> DomainState.Recording
            EntityState.PAUSED -> DomainState.Paused
            EntityState.PROCESSING -> DomainState.Processing(0f)
            EntityState.COMPLETED -> DomainState.Completed("", 0L)
            EntityState.ERROR -> DomainState.Error(Throwable("Unknown error"))
            EntityState.CANCELLED -> DomainState.Cancelled()
        }
    }
} 