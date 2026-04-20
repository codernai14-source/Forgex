package com.forgex.mobile.core.common.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class LanguageMode {
    FOLLOW_SYSTEM,
    MANUAL
}

object AppLanguage {
    const val DEFAULT_LANGUAGE_TAG = "zh-CN"

    fun normalize(raw: String?): String {
        val value = raw?.trim()
            ?.substringBefore(',')
            ?.substringBefore(';')
            ?.replace('_', '-')
            .orEmpty()
        if (value.isBlank()) return DEFAULT_LANGUAGE_TAG
        return when {
            value.equals("zh", ignoreCase = true) || value.equals("zh-cn", ignoreCase = true) -> "zh-CN"
            value.equals("zh-tw", ignoreCase = true) || value.equals("zh-hk", ignoreCase = true) -> "zh-TW"
            value.equals("en", ignoreCase = true) || value.equals("en-us", ignoreCase = true) -> "en-US"
            else -> value
        }
    }

    fun currentSystemLanguage(): String = normalize(Locale.getDefault().toLanguageTag())

    fun toLocaleListCompat(languageTag: String?): LocaleListCompat {
        val normalized = languageTag?.takeIf { it.isNotBlank() }?.let(::normalize).orEmpty()
        return if (normalized.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(normalized)
        }
    }

    fun apply(languageMode: LanguageMode, languageTag: String?) {
        val locales = if (languageMode == LanguageMode.FOLLOW_SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            toLocaleListCompat(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun displayName(languageTag: String, displayLanguage: String? = null): String {
        val locale = Locale.forLanguageTag(normalize(languageTag))
        val targetLocale = displayLanguage?.let { Locale.forLanguageTag(normalize(it)) } ?: locale
        return locale.getDisplayName(targetLocale).replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(targetLocale) else ch.toString()
        }
    }
}
