package com.example.clicknote.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore
) {
    // Map of language codes to their native names
    private val supportedLanguages = mapOf(
        "af" to "Afrikaans",
        "sq" to "Shqip",
        "am" to "አማርኛ",
        "ar" to "العربية",
        "hy" to "Հայերեն",
        "as" to "অসমীয়া",
        "ay" to "Aymar aru",
        "az" to "Azərbaycan dili",
        "bm" to "Bamanankan",
        "eu" to "Euskara",
        "be" to "Беларуская",
        "bn" to "বাংলা",
        "bho" to "भोजपुरी",
        "bs" to "Bosanski",
        "bg" to "Български",
        "ca" to "Català",
        "ceb" to "Cebuano",
        "ny" to "Chichewa",
        "zh" to "中文",
        "co" to "Corsu",
        "hr" to "Hrvatski",
        "cs" to "Čeština",
        "da" to "Dansk",
        "dv" to "ދިވެހި",
        "doi" to "डोगरी",
        "nl" to "Nederlands",
        "en" to "English",
        "eo" to "Esperanto",
        "et" to "Eesti",
        "ee" to "Eʋegbe",
        "fil" to "Filipino",
        "fi" to "Suomi",
        "fr" to "Français",
        "fy" to "Frysk",
        "gl" to "Galego",
        "ka" to "ქართული",
        "de" to "Deutsch",
        "el" to "Ελληνικά",
        "gn" to "Avañe'ẽ",
        "gu" to "ગુજરાતી",
        "ht" to "Kreyòl ayisyen",
        "ha" to "Hausa",
        "haw" to "ʻŌlelo Hawaiʻi",
        "he" to "עברית",
        "hi" to "हिन्दी",
        "hmn" to "Hmong",
        "hu" to "Magyar",
        "is" to "Íslenska",
        "ig" to "Igbo",
        "ilo" to "Ilokano",
        "id" to "Bahasa Indonesia",
        "ga" to "Gaeilge",
        "it" to "Italiano",
        "ja" to "日本語",
        "jv" to "Basa Jawa",
        "kn" to "ಕನ್ನಡ",
        "kk" to "Қазақ тілі",
        "km" to "ខ្មែរ",
        "rw" to "Kinyarwanda",
        "gom" to "कोंकणी",
        "ko" to "한국어",
        "kri" to "Krio",
        "ku" to "Kurdî",
        "ckb" to "کوردیی ناوەندی",
        "ky" to "Кыргызча",
        "lo" to "ລາວ",
        "la" to "Latina",
        "lv" to "Latviešu",
        "ln" to "Lingála",
        "lt" to "Lietuvių",
        "lg" to "Luganda",
        "lb" to "Lëtzebuergesch",
        "mk" to "Македонски",
        "mai" to "मैथिली",
        "mg" to "Malagasy",
        "ms" to "Bahasa Melayu",
        "ml" to "മലയാളം",
        "mt" to "Malti",
        "mi" to "Māori",
        "mr" to "मराठी",
        "mni-Mtei" to "মৈতৈলোন্",
        "lus" to "Mizo ṭawng",
        "mn" to "Монгол",
        "my" to "မြန်မာ",
        "ne" to "नेपाली",
        "no" to "Norsk",
        "or" to "ଓଡ଼ିଆ",
        "om" to "Afaan Oromoo",
        "ps" to "پښتو",
        "fa" to "فارسی",
        "pl" to "Polski",
        "pt" to "Português",
        "pa" to "ਪੰਜਾਬੀ",
        "qu" to "Runasimi",
        "ro" to "Română",
        "ru" to "Русский",
        "sm" to "Gagana Samoa",
        "sa" to "संस्कृत",
        "gd" to "Gàidhlig",
        "nso" to "Sepedi",
        "sr" to "Српски",
        "st" to "Sesotho",
        "sn" to "ChiShona",
        "sd" to "سنڌي",
        "si" to "සිංහල",
        "sk" to "Slovenčina",
        "sl" to "Slovenščina",
        "so" to "Soomaali",
        "es" to "Español",
        "su" to "Basa Sunda",
        "sw" to "Kiswahili",
        "sv" to "Svenska",
        "tl" to "Tagalog",
        "tg" to "Тоҷикӣ",
        "ta" to "தமிழ்",
        "tt" to "Татар",
        "te" to "తెలుగు",
        "th" to "ไทย",
        "ti" to "ትግርኛ",
        "ts" to "Xitsonga",
        "tr" to "Türkçe",
        "tk" to "Türkmen dili",
        "ak" to "Twi",
        "uk" to "Українська",
        "ur" to "اردو",
        "ug" to "ئۇيغۇرچە",
        "uz" to "O'zbek",
        "vi" to "Tiếng Việt",
        "cy" to "Cymraeg",
        "xh" to "isiXhosa",
        "yi" to "ייִדיש",
        "yo" to "Yorùbá",
        "zu" to "isiZulu"
    )

    suspend fun setLanguage(languageCode: String) {
        if (languageCode in supportedLanguages.keys) {
            val localeList = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(localeList)
            userPreferences.setLanguage(languageCode)
        }
    }

    suspend fun getCurrentLanguage(): String {
        return userPreferences.language.first()
    }

    fun getSupportedLanguages(): Map<String, String> = supportedLanguages

    fun getLanguageGroups(): Map<String, List<Pair<String, String>>> {
        return supportedLanguages.entries.groupBy { (code, _) ->
            when {
                isRTL(code) -> "RTL Languages"
                code in ASIAN_LANGUAGES -> "Asian Languages"
                code in EUROPEAN_LANGUAGES -> "European Languages"
                code in AFRICAN_LANGUAGES -> "African Languages"
                else -> "Other Languages"
            }
        }.mapValues { (_, entries) ->
            entries.map { it.key to it.value }.sortedBy { it.second }
        }
    }

    fun isRTL(languageCode: String): Boolean {
        return RTL_LANGUAGES.contains(languageCode)
    }

    fun isCurrentRTL(): Boolean {
        val currentLocale = getCurrentLocale()
        return isRTL(currentLocale.language)
    }

    fun getDeviceLanguage(): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
        return locale.language
    }

    fun formatNumber(number: Number, locale: Locale = getCurrentLocale()): String {
        return NumberFormat.getInstance(locale).format(number)
    }

    fun formatCurrency(amount: Double, currencyCode: String = "GBP", locale: Locale = getCurrentLocale()): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = Currency.getInstance(currencyCode)
        return format.format(amount)
    }

    fun formatDate(date: Date, locale: Locale = getCurrentLocale()): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
        return dateFormat.format(date)
    }

    private fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    companion object {
        private val RTL_LANGUAGES = setOf("ar", "dv", "fa", "he", "ps", "sd", "ur", "ug")
        private val ASIAN_LANGUAGES = setOf("zh", "ja", "ko", "th", "vi", "km", "my", "lo", "ka")
        private val EUROPEAN_LANGUAGES = setOf("en", "fr", "de", "it", "es", "pt", "ru", "pl", "nl", "cs", "sv", "da", "fi", "no", "hu", "ro", "bg", "el", "sk", "hr")
        private val AFRICAN_LANGUAGES = setOf("am", "ha", "ig", "rw", "so", "sw", "yo", "zu", "xh", "st", "ny")
        private const val TAG = "LocalizationManager"
    }
} 