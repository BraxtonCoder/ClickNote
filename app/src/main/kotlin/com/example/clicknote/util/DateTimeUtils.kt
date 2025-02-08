package com.example.clicknote.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DateTimeUtils {
    /**
     * Converts a LocalDateTime to a Unix timestamp in milliseconds
     */
    fun toTimestamp(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Converts a Unix timestamp in milliseconds to LocalDateTime
     */
    fun fromTimestamp(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
    }

    /**
     * Gets the current timestamp in milliseconds
     */
    fun currentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Gets the current LocalDateTime
     */
    fun now(): LocalDateTime {
        return LocalDateTime.now()
    }

    /**
     * Checks if a timestamp is older than the specified number of days
     */
    fun isOlderThanDays(timestamp: Long, days: Int): Boolean {
        val now = currentTimestamp()
        val daysInMillis = days * 24 * 60 * 60 * 1000L
        return (now - timestamp) > daysInMillis
    }

    /**
     * Formats a timestamp into a human-readable string
     */
    fun formatTimestamp(timestamp: Long): String {
        val dateTime = fromTimestamp(timestamp)
        return dateTime.toString() // You can customize this format as needed
    }
} 