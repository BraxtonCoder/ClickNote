package com.example.clicknote.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.clicknote.domain.model.TranscriptionLanguage
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
    private val supportedLanguages = TranscriptionLanguage.values().associate { 
        it.code to it.displayName 
    }

    suspend fun setLanguage(language: TranscriptionLanguage) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
        userPreferences.setTranscriptionLanguage(language)
    }

    suspend fun getCurrentLanguage(): TranscriptionLanguage {
        return userPreferences.transcriptionLanguage.first()
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

    fun getDeviceLanguage(): TranscriptionLanguage {
        val locale = LocaleListCompat.getAdjustedDefault()[0]
        return TranscriptionLanguage.fromCode(locale.language) ?: TranscriptionLanguage.ENGLISH
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