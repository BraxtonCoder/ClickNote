package com.example.clicknote.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

object DateTimeUtils {
    /**
     * Converts a timestamp (milliseconds since epoch) to LocalDateTime
     */
    fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneOffset.UTC
        )
    }

    /**
     * Converts a LocalDateTime to timestamp (milliseconds since epoch)
     */
    fun localDateTimeToTimestamp(dateTime: LocalDateTime): Long {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    /**
     * Gets the current timestamp in milliseconds
     */
    fun currentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Gets the current LocalDateTime in UTC
     */
    fun currentDateTime(): LocalDateTime {
        return LocalDateTime.now(ZoneOffset.UTC)
    }
} 