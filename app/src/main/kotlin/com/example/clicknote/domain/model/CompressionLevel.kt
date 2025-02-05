package com.example.clicknote.domain.model

/**
 * Enum representing different levels of compression for backups
 */
enum class CompressionLevel(val level: Int) {
    NONE(0),
    FAST(1),
    BALANCED(6),
    MAXIMUM(9);

    companion object {
        fun fromLevel(level: Int): CompressionLevel {
            return values().find { it.level == level } ?: BALANCED
        }
    }
} 