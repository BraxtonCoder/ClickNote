package com.example.clicknote.domain.model

/**
 * Represents a language supported for transcription
 */
data class TranscriptionLanguage(
    val code: String,
    val name: String,
    val isSupported: Boolean = true
) {
    companion object {
        val DEFAULT = TranscriptionLanguage("en", "English")
        val ENGLISH = TranscriptionLanguage("en", "English")
        val SPANISH = TranscriptionLanguage("es", "Spanish")
        val FRENCH = TranscriptionLanguage("fr", "French")
        val GERMAN = TranscriptionLanguage("de", "German")
        val ITALIAN = TranscriptionLanguage("it", "Italian")
        val PORTUGUESE = TranscriptionLanguage("pt", "Portuguese")
        val DUTCH = TranscriptionLanguage("nl", "Dutch")
        val POLISH = TranscriptionLanguage("pl", "Polish")
        val RUSSIAN = TranscriptionLanguage("ru", "Russian")
        val JAPANESE = TranscriptionLanguage("ja", "Japanese")
        val KOREAN = TranscriptionLanguage("ko", "Korean")
        val CHINESE = TranscriptionLanguage("zh", "Chinese")
        val ARABIC = TranscriptionLanguage("ar", "Arabic")
        val HINDI = TranscriptionLanguage("hi", "Hindi")

        fun fromCode(code: String): TranscriptionLanguage {
            return when (code.lowercase()) {
                "en" -> ENGLISH
                "es" -> SPANISH
                "fr" -> FRENCH
                "de" -> GERMAN
                "it" -> ITALIAN
                "pt" -> PORTUGUESE
                "nl" -> DUTCH
                "pl" -> POLISH
                "ru" -> RUSSIAN
                "ja" -> JAPANESE
                "ko" -> KOREAN
                "zh" -> CHINESE
                "ar" -> ARABIC
                "hi" -> HINDI
                else -> TranscriptionLanguage(code, "Unknown", false)
            }
        }

        fun isSupported(code: String): Boolean {
            return fromCode(code).isSupported
        }

        fun getAllSupported(): List<TranscriptionLanguage> {
            return listOf(
                ENGLISH,
                SPANISH,
                FRENCH,
                GERMAN,
                ITALIAN,
                PORTUGUESE,
                DUTCH,
                POLISH,
                RUSSIAN,
                JAPANESE,
                KOREAN,
                CHINESE,
                ARABIC,
                HINDI
            ).filter { it.isSupported }
        }
    }
} 