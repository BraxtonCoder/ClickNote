package com.example.clicknote.domain.model

import java.time.LocalDate

sealed class TimeFilter {
    data object ALL : TimeFilter()
    data object TODAY : TimeFilter()
    data object LAST_7_DAYS : TimeFilter()
    data object LAST_30_DAYS : TimeFilter()
    data object LAST_3_MONTHS : TimeFilter()
    data object LAST_6_MONTHS : TimeFilter()
    data object LAST_YEAR : TimeFilter()
    data class CUSTOM(val startDate: LocalDate, val endDate: LocalDate) : TimeFilter()

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
} 