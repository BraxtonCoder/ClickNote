package com.example.clicknote.data

enum class TimeFilter(val displayName: String) {
    ALL("All Notes"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    LAST_YEAR("Last Year"),
    CUSTOM("Custom Range");

    companion object {
        fun fromDisplayName(name: String): TimeFilter {
            return values().find { it.displayName == name } ?: ALL
        }
    }
} 