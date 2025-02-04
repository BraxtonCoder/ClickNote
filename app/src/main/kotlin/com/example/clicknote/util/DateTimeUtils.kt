package com.example.clicknote.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DateTimeUtils {
    fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
    }

    fun localDateTimeToTimestamp(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
} 