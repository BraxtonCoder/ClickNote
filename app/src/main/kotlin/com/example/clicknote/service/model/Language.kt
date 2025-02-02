package com.example.clicknote.service.model

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    DUTCH("nl", "Dutch"),
    POLISH("pl", "Polish"),
    RUSSIAN("ru", "Russian"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    CHINESE("zh", "Chinese"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    BENGALI("bn", "Bengali"),
    TURKISH("tr", "Turkish"),
    VIETNAMESE("vi", "Vietnamese"),
    THAI("th", "Thai"),
    INDONESIAN("id", "Indonesian"),
    MALAY("ms", "Malay"),
    AUTO_DETECT("auto", "Auto Detect");

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code.lowercase() } ?: AUTO_DETECT
        }

        fun fromDisplayName(name: String): Language {
            return values().find { it.displayName.equals(name, ignoreCase = true) } ?: AUTO_DETECT
        }
    }
} 