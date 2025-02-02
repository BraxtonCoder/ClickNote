package com.example.clicknote.domain.model

enum class Language(val code: String, val displayName: String) {
    ALL("all", "All Languages"),
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
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    BENGALI("bn", "Bengali"),
    DUTCH("nl", "Dutch"),
    POLISH("pl", "Polish"),
    TURKISH("tr", "Turkish"),
    VIETNAMESE("vi", "Vietnamese"),
    THAI("th", "Thai"),
    INDONESIAN("id", "Indonesian"),
    MALAY("ms", "Malay"),
    PERSIAN("fa", "Persian"),
    HEBREW("he", "Hebrew"),
    GREEK("el", "Greek"),
    CZECH("cs", "Czech"),
    SWEDISH("sv", "Swedish"),
    DANISH("da", "Danish"),
    FINNISH("fi", "Finnish"),
    NORWEGIAN("no", "Norwegian"),
    HUNGARIAN("hu", "Hungarian"),
    ROMANIAN("ro", "Romanian"),
    BULGARIAN("bg", "Bulgarian"),
    UKRAINIAN("uk", "Ukrainian"),
    CROATIAN("hr", "Croatian"),
    SERBIAN("sr", "Serbian"),
    SLOVAK("sk", "Slovak"),
    SLOVENIAN("sl", "Slovenian"),
    LITHUANIAN("lt", "Lithuanian"),
    LATVIAN("lv", "Latvian"),
    ESTONIAN("et", "Estonian"),
    ALBANIAN("sq", "Albanian"),
    MACEDONIAN("mk", "Macedonian"),
    MALTESE("mt", "Maltese"),
    ICELANDIC("is", "Icelandic"),
    WELSH("cy", "Welsh"),
    IRISH("ga", "Irish"),
    SCOTS_GAELIC("gd", "Scots Gaelic"),
    SWAHILI("sw", "Swahili"),
    AFRIKAANS("af", "Afrikaans"),
    ZULU("zu", "Zulu"),
    XHOSA("xh", "Xhosa"),
    YORUBA("yo", "Yoruba"),
    IGBO("ig", "Igbo"),
    HAUSA("ha", "Hausa"),
    AMHARIC("am", "Amharic"),
    SOMALI("so", "Somali"),
    MAORI("mi", "Maori"),
    HAWAIIAN("haw", "Hawaiian"),
    MONGOLIAN("mn", "Mongolian"),
    KHMER("km", "Khmer"),
    LAO("lo", "Lao");

    companion object {
        val COMMON = listOf(
            ALL,
            ENGLISH,
            SPANISH,
            FRENCH,
            GERMAN,
            ITALIAN,
            PORTUGUESE,
            RUSSIAN,
            JAPANESE,
            KOREAN,
            CHINESE
        )

        val EUROPEAN = listOf(
            ENGLISH, SPANISH, FRENCH, GERMAN, ITALIAN,
            PORTUGUESE, RUSSIAN, DUTCH, POLISH, GREEK
        )

        val ASIAN = listOf(
            JAPANESE, KOREAN, CHINESE, VIETNAMESE,
            THAI, INDONESIAN, MALAY, KHMER, LAO
        )

        val MIDDLE_EASTERN = listOf(
            ARABIC, PERSIAN, HEBREW, TURKISH
        )

        val INDIAN = listOf(
            HINDI, BENGALI
        )

        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ALL
        }

        fun fromDisplayName(name: String): Language {
            return values().find { it.displayName == name } ?: ALL
        }
    }
} 