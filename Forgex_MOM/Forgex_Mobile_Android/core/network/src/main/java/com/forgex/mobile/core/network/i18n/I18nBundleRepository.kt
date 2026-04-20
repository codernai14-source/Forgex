package com.forgex.mobile.core.network.i18n

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.api.I18nApi
import com.forgex.mobile.core.network.model.i18n.MobileI18nBundleRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class I18nBundleRepository @Inject constructor(
    private val i18nApi: I18nApi,
    private val sessionStore: SessionStore
) {
    private val gson = Gson()
    private val type = object : TypeToken<Map<String, String>>() {}.type

    suspend fun fetchBundle(languageTag: String): AppResult<Map<String, String>> {
        return try {
            val normalized = AppLanguage.normalize(languageTag)
            val response = i18nApi.getMobileBundle(MobileI18nBundleRequest(langCode = normalized))
            if (response.isSuccess()) {
                val bundle = response.data.orEmpty()
                sessionStore.saveI18nBundle(normalized, gson.toJson(bundle))
                AppResult.Success(bundle)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load i18n bundle failed")
        }
    }

    suspend fun readCachedBundle(languageTag: String): Map<String, String> {
        val cachedLanguage = sessionStore.i18nBundleLanguageTag.first()
        val cachedJson = sessionStore.i18nBundleJson.first()
        if (cachedLanguage.isNullOrBlank() || cachedJson.isNullOrBlank()) {
            return emptyMap()
        }
        if (AppLanguage.normalize(languageTag) != AppLanguage.normalize(cachedLanguage)) {
            return emptyMap()
        }
        return runCatching {
            gson.fromJson<Map<String, String>>(cachedJson, type).orEmpty()
        }.getOrDefault(emptyMap())
    }
}
