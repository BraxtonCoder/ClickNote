package com.example.clicknote.data

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class DateFilter {
    ALL,
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_3_MONTHS,
    LAST_6_MONTHS,
    LAST_YEAR;

    fun getStartDate(): LocalDateTime {
        val now = LocalDateTime.now()
        return when (this) {
            ALL -> LocalDateTime.MIN
            TODAY -> now.truncatedTo(ChronoUnit.DAYS)
            LAST_7_DAYS -> now.minusDays(7)
            LAST_30_DAYS -> now.minusDays(30)
            LAST_3_MONTHS -> now.minusMonths(3)
            LAST_6_MONTHS -> now.minusMonths(6)
            LAST_YEAR -> now.minusYears(1)
        }
    }

    fun getDisplayName(): String = when (this) {
        ALL -> "All"
        TODAY -> "Today"
        LAST_7_DAYS -> "Last 7 days"
        LAST_30_DAYS -> "Last 30 days"
        LAST_3_MONTHS -> "Last 3 months"
        LAST_6_MONTHS -> "Last 6 months"
        LAST_YEAR -> "Last year"
    }
} 