package com.forgex.mobile.core.network.i18n

import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.common.i18n.LanguageMode
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.api.I18nApi
import com.forgex.mobile.core.network.model.i18n.LanguageType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class LanguageRepository @Inject constructor(
    private val i18nApi: I18nApi,
    private val sessionStore: SessionStore
) {

    suspend fun resolveStartupLanguage(): ResolvedLanguage {
        val availableLanguages = loadEnabledLanguages().getOrElse {
            defaultAvailableLanguages()
        }
        val storedMode = sessionStore.languageMode.first()
        val storedTag = sessionStore.languageTag.first()
        val defaultLanguage = loadDefaultLanguage().getOrNull()
            ?: availableLanguages.firstOrNull { it.isDefault }
            ?: availableLanguages.firstOrNull()
            ?: LanguageType(
                langCode = AppLanguage.DEFAULT_LANGUAGE_TAG,
                langName = "简体中文",
                langNameEn = "Simplified Chinese",
                enabled = true,
                isDefault = true
            )

        val resolvedTag = when (storedMode) {
            LanguageMode.MANUAL -> {
                val manual = storedTag?.let(AppLanguage::normalize)
                availableLanguages.firstOrNull { it.langCode == manual }?.langCode
                    ?: defaultLanguage.langCode
            }
            LanguageMode.FOLLOW_SYSTEM -> {
                val systemLanguage = AppLanguage.currentSystemLanguage()
                availableLanguages.firstOrNull { it.langCode == systemLanguage }?.langCode
                    ?: defaultLanguage.langCode
            }
        }

        sessionStore.saveLastResolvedLanguageTag(resolvedTag)
        return ResolvedLanguage(
            languageMode = storedMode,
            languageTag = resolvedTag,
            availableLanguages = availableLanguages,
            defaultLanguage = defaultLanguage
        )
    }

    suspend fun loadEnabledLanguages(): Result<List<LanguageType>> {
        return runCatching {
            val response = i18nApi.listEnabledLanguages()
            if (!response.isSuccess()) {
                throw IllegalStateException(response.errorMessage())
            }
            response.data.orEmpty()
                .filter { it.enabled }
                .sortedBy { it.orderNum ?: Int.MAX_VALUE }
                .map { it.copy(langCode = AppLanguage.normalize(it.langCode)) }
        }
    }

    suspend fun loadDefaultLanguage(): Result<LanguageType> {
        return runCatching {
            val response = i18nApi.getDefaultLanguage()
            val data = response.data
            if (!response.isSuccess() || data == null) {
                throw IllegalStateException(response.errorMessage())
            }
            data.copy(langCode = AppLanguage.normalize(data.langCode))
        }
    }

    suspend fun saveSelection(mode: LanguageMode, languageTag: String?) {
        sessionStore.saveLanguageSelection(mode, languageTag)
        val resolvedTag = if (mode == LanguageMode.FOLLOW_SYSTEM) {
            AppLanguage.currentSystemLanguage()
        } else {
            AppLanguage.normalize(languageTag)
        }
        sessionStore.saveLastResolvedLanguageTag(resolvedTag)
    }

    private fun defaultAvailableLanguages(): List<LanguageType> {
        return listOf(
            LanguageType(
                langCode = "zh-CN",
                langName = "简体中文",
                langNameEn = "Simplified Chinese",
                enabled = true,
                isDefault = true,
                orderNum = 1
            ),
            LanguageType(
                langCode = "en-US",
                langName = "English",
                langNameEn = "English",
                enabled = true,
                isDefault = false,
                orderNum = 2
            )
        )
    }
}

data class ResolvedLanguage(
    val languageMode: LanguageMode,
    val languageTag: String,
    val availableLanguages: List<LanguageType>,
    val defaultLanguage: LanguageType
)
