package com.example.clicknote.domain.model

enum class TranscriptionLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    DUTCH("nl", "Dutch"),
    POLISH("pl", "Polish"),
    RUSSIAN("ru", "Russian"),
    UKRAINIAN("uk", "Ukrainian"),
    CHINESE("zh", "Chinese"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    BENGALI("bn", "Bengali"),
    TURKISH("tr", "Turkish"),
    VIETNAMESE("vi", "Vietnamese"),
    THAI("th", "Thai"),
    INDONESIAN("id", "Indonesian");

    companion object {
        val COMMON = listOf(ENGLISH, SPANISH, FRENCH, GERMAN, ITALIAN)
        val EUROPEAN = listOf(PORTUGUESE, DUTCH, POLISH, RUSSIAN, UKRAINIAN)
        val ASIAN = listOf(CHINESE, JAPANESE, KOREAN, VIETNAMESE, THAI)
        val MIDDLE_EASTERN = listOf(ARABIC, TURKISH)
        val INDIAN = listOf(HINDI, BENGALI)

        fun fromCode(code: String): TranscriptionLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }

        fun fromDisplayName(name: String): TranscriptionLanguage {
            return values().find { it.displayName == name } ?: ENGLISH
        }
    }
} 