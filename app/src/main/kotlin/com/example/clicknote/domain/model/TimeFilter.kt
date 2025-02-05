package com.example.clicknote.domain.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

sealed class TimeFilter {
    object ALL : TimeFilter()
    object TODAY : TimeFilter()
    object LAST_7_DAYS : TimeFilter()
    object LAST_30_DAYS : TimeFilter()
    object LAST_3_MONTHS : TimeFilter()
    object LAST_6_MONTHS : TimeFilter()
    object LAST_YEAR : TimeFilter()
    data class CUSTOM(val startDate: LocalDateTime, val endDate: LocalDateTime) : TimeFilter()

    fun getDisplayName(): String = when (this) {
        ALL -> "All Time"
        TODAY -> "Today"
        LAST_7_DAYS -> "Last 7 Days"
        LAST_30_DAYS -> "Last 30 Days"
        LAST_3_MONTHS -> "Last 3 Months"
        LAST_6_MONTHS -> "Last 6 Months"
        LAST_YEAR -> "Last Year"
        is CUSTOM -> "Custom Range"
    }

    companion object {
        fun values(): List<TimeFilter> = listOf(
            ALL,
            TODAY,
            LAST_7_DAYS,
            LAST_30_DAYS,
            LAST_3_MONTHS,
            LAST_6_MONTHS,
            LAST_YEAR
        )

        fun getStartDate(filter: TimeFilter): LocalDateTime? {
            val now = LocalDateTime.now()
            return when (filter) {
                is ALL -> null
                is TODAY -> now.truncatedTo(ChronoUnit.DAYS)
                is LAST_7_DAYS -> now.minusDays(7)
                is LAST_30_DAYS -> now.minusDays(30)
                is LAST_3_MONTHS -> now.minusMonths(3)
                is LAST_6_MONTHS -> now.minusMonths(6)
                is LAST_YEAR -> now.minusYears(1)
                is CUSTOM -> filter.startDate
            }
        }

        fun getEndDate(filter: TimeFilter): LocalDateTime? {
            return when (filter) {
                is ALL -> null
                is CUSTOM -> filter.endDate
                else -> LocalDateTime.now()
            }
        }
    }
} 