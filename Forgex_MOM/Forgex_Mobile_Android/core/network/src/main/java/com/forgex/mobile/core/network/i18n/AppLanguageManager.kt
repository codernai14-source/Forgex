package com.forgex.mobile.core.network.i18n

import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.common.i18n.LanguageMode
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AppLanguageManager @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val i18nBundleRepository: I18nBundleRepository,
    private val sessionStore: SessionStore
) {
    private val _state = MutableStateFlow(AppLanguageState())
    val state: StateFlow<AppLanguageState> = _state.asStateFlow()

    val currentLanguageTag: Flow<String> = sessionStore.lastResolvedLanguageTag
        .map { value -> value ?: AppLanguage.DEFAULT_LANGUAGE_TAG }

    suspend fun initialize() {
        val resolved = languageRepository.resolveStartupLanguage()
        AppLanguage.apply(resolved.languageMode, resolved.languageTag)
        val bundle = i18nBundleRepository.readCachedBundle(resolved.languageTag)
        _state.value = AppLanguageState(
            mode = resolved.languageMode,
            currentLanguageTag = resolved.languageTag,
            availableLanguages = resolved.availableLanguages,
            defaultLanguageTag = resolved.defaultLanguage.langCode,
            bundle = bundle
        )
        fetchBundle(resolved.languageTag)
    }

    suspend fun followSystem() {
        val resolved = languageRepository.resolveStartupLanguage()
        languageRepository.saveSelection(LanguageMode.FOLLOW_SYSTEM, null)
        AppLanguage.apply(LanguageMode.FOLLOW_SYSTEM, null)
        updateState(
            mode = LanguageMode.FOLLOW_SYSTEM,
            languageTag = resolved.languageTag,
            availableLanguages = resolved.availableLanguages,
            defaultLanguageTag = resolved.defaultLanguage.langCode
        )
        fetchBundle(resolved.languageTag)
    }

    suspend fun selectLanguage(languageTag: String) {
        val normalized = AppLanguage.normalize(languageTag)
        languageRepository.saveSelection(LanguageMode.MANUAL, normalized)
        AppLanguage.apply(LanguageMode.MANUAL, normalized)
        updateState(
            mode = LanguageMode.MANUAL,
            languageTag = normalized,
            availableLanguages = _state.value.availableLanguages,
            defaultLanguageTag = _state.value.defaultLanguageTag
        )
        fetchBundle(normalized)
    }

    suspend fun fetchBundle(languageTag: String? = null): AppResult<Map<String, String>> {
        val normalized = AppLanguage.normalize(languageTag ?: currentLanguageTag.first())
        return when (val result = i18nBundleRepository.fetchBundle(normalized)) {
            is AppResult.Success -> {
                _state.value = _state.value.copy(bundle = result.data, currentLanguageTag = normalized)
                result
            }
            is AppResult.Error -> {
                val cached = i18nBundleRepository.readCachedBundle(normalized)
                if (cached.isNotEmpty()) {
                    _state.value = _state.value.copy(bundle = cached, currentLanguageTag = normalized)
                    AppResult.Success(cached)
                } else {
                    result
                }
            }
            AppResult.Loading -> AppResult.Loading
        }
    }

    fun resolveText(key: String, fallback: String? = null): String? {
        return _state.value.bundle[key]?.takeIf { it.isNotBlank() } ?: fallback
    }

    fun observeUiState(): Flow<AppLanguageState> {
        return combine(state, sessionStore.lastResolvedLanguageTag) { current, resolved ->
            current.copy(currentLanguageTag = resolved ?: current.currentLanguageTag)
        }
    }

    private fun updateState(
        mode: LanguageMode,
        languageTag: String,
        availableLanguages: List<com.forgex.mobile.core.network.model.i18n.LanguageType>,
        defaultLanguageTag: String
    ) {
        _state.value = _state.value.copy(
            mode = mode,
            currentLanguageTag = languageTag,
            availableLanguages = availableLanguages,
            defaultLanguageTag = defaultLanguageTag
        )
    }
}

data class AppLanguageState(
    val mode: LanguageMode = LanguageMode.FOLLOW_SYSTEM,
    val currentLanguageTag: String = AppLanguage.DEFAULT_LANGUAGE_TAG,
    val availableLanguages: List<com.forgex.mobile.core.network.model.i18n.LanguageType> = emptyList(),
    val defaultLanguageTag: String = AppLanguage.DEFAULT_LANGUAGE_TAG,
    val bundle: Map<String, String> = emptyMap()
)
