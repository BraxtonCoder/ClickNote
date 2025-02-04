package com.example.clicknote.domain.model

enum class TranscriptionLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    RUSSIAN("ru", "Russian"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    CHINESE("zh", "Chinese"),
    HINDI("hi", "Hindi"),
    ARABIC("ar", "Arabic");

    companion object {
        fun fromCode(code: String): TranscriptionLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
} 