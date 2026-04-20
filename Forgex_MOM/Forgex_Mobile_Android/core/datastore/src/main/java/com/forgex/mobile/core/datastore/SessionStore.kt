package com.forgex.mobile.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.common.i18n.LanguageMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(
    context: Context
) {

    private val dataStore = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
    )

    val token: Flow<String?> = preferenceFlow(TOKEN_KEY)

    val tenantId: Flow<String?> = preferenceFlow(TENANT_ID_KEY)

    val account: Flow<String?> = preferenceFlow(ACCOUNT_KEY)

    val systemName: Flow<String?> = preferenceFlow(SYSTEM_NAME_KEY)

    val serverHost: Flow<String?> = preferenceFlow(SERVER_HOST_KEY)

    val serverScheme: Flow<String?> = preferenceFlow(SERVER_SCHEME_KEY)

    val languageMode: Flow<LanguageMode> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences ->
            when (preferences[LANGUAGE_MODE_KEY]) {
                LanguageMode.MANUAL.name -> LanguageMode.MANUAL
                else -> LanguageMode.FOLLOW_SYSTEM
            }
        }

    val languageTag: Flow<String?> = preferenceFlow(LANGUAGE_TAG_KEY)
        .map { value -> value?.let(AppLanguage::normalize) }

    val lastResolvedLanguageTag: Flow<String?> = preferenceFlow(LAST_RESOLVED_LANGUAGE_TAG_KEY)
        .map { value -> value?.let(AppLanguage::normalize) }

    val i18nBundleJson: Flow<String?> = preferenceFlow(I18N_BUNDLE_JSON_KEY)

    val i18nBundleLanguageTag: Flow<String?> = preferenceFlow(I18N_BUNDLE_LANGUAGE_TAG_KEY)
        .map { value -> value?.let(AppLanguage::normalize) }

    val serverPort: Flow<Int?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[SERVER_PORT_KEY] }

    val serverEndpoint: Flow<ServerEndpointConfig?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences ->
            val host = preferences[SERVER_HOST_KEY]?.trim().orEmpty()
            if (host.isBlank()) {
                null
            } else {
                ServerEndpointConfig(
                    host = host,
                    port = preferences[SERVER_PORT_KEY] ?: 9000,
                    scheme = preferences[SERVER_SCHEME_KEY]?.ifBlank { "http" } ?: "http"
                )
            }
        }

    suspend fun saveSession(account: String, tenantId: String, token: String? = null) {
        dataStore.edit { preferences ->
            preferences[ACCOUNT_KEY] = account
            preferences[TENANT_ID_KEY] = tenantId
            if (!token.isNullOrBlank()) {
                preferences[TOKEN_KEY] = token
            } else {
                preferences.remove(TOKEN_KEY)
            }
        }
    }

    suspend fun saveTenantId(tenantId: String) {
        dataStore.edit { preferences ->
            preferences[TENANT_ID_KEY] = tenantId
        }
    }

    suspend fun saveSystemName(value: String) {
        dataStore.edit { preferences ->
            if (value.isBlank()) {
                preferences.remove(SYSTEM_NAME_KEY)
            } else {
                preferences[SYSTEM_NAME_KEY] = value
            }
        }
    }

    suspend fun saveServerEndpoint(
        host: String,
        port: Int,
        scheme: String = "http"
    ) {
        dataStore.edit { preferences ->
            preferences[SERVER_HOST_KEY] = host.trim()
            preferences[SERVER_PORT_KEY] = port
            preferences[SERVER_SCHEME_KEY] = scheme.trim().ifBlank { "http" }
        }
    }

    suspend fun clearServerEndpoint() {
        dataStore.edit { preferences ->
            preferences.remove(SERVER_HOST_KEY)
            preferences.remove(SERVER_PORT_KEY)
            preferences.remove(SERVER_SCHEME_KEY)
        }
    }

    suspend fun saveLanguageSelection(
        languageMode: LanguageMode,
        languageTag: String?
    ) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_MODE_KEY] = languageMode.name
            if (languageTag.isNullOrBlank()) {
                preferences.remove(LANGUAGE_TAG_KEY)
            } else {
                preferences[LANGUAGE_TAG_KEY] = AppLanguage.normalize(languageTag)
            }
        }
    }

    suspend fun saveLastResolvedLanguageTag(languageTag: String) {
        dataStore.edit { preferences ->
            preferences[LAST_RESOLVED_LANGUAGE_TAG_KEY] = AppLanguage.normalize(languageTag)
        }
    }

    suspend fun saveI18nBundle(languageTag: String, bundleJson: String) {
        dataStore.edit { preferences ->
            preferences[I18N_BUNDLE_LANGUAGE_TAG_KEY] = AppLanguage.normalize(languageTag)
            preferences[I18N_BUNDLE_JSON_KEY] = bundleJson
        }
    }

    suspend fun clearI18nBundle() {
        dataStore.edit { preferences ->
            preferences.remove(I18N_BUNDLE_LANGUAGE_TAG_KEY)
            preferences.remove(I18N_BUNDLE_JSON_KEY)
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(TENANT_ID_KEY)
            preferences.remove(ACCOUNT_KEY)
        }
    }

    private fun preferenceFlow(key: androidx.datastore.preferences.core.Preferences.Key<String>): Flow<String?> {
        return dataStore.data
            .catch {
                if (it is IOException) emit(emptyPreferences()) else throw it
            }
            .map { preferences -> preferences[key] }
    }

    companion object {
        private const val DATASTORE_NAME = "forgex_mobile_session.preferences_pb"
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val TENANT_ID_KEY = stringPreferencesKey("tenant_id")
        private val ACCOUNT_KEY = stringPreferencesKey("account")
        private val SYSTEM_NAME_KEY = stringPreferencesKey("system_name")
        private val SERVER_HOST_KEY = stringPreferencesKey("server_host")
        private val SERVER_SCHEME_KEY = stringPreferencesKey("server_scheme")
        private val LANGUAGE_MODE_KEY = stringPreferencesKey("language_mode")
        private val LANGUAGE_TAG_KEY = stringPreferencesKey("language_tag")
        private val LAST_RESOLVED_LANGUAGE_TAG_KEY = stringPreferencesKey("last_resolved_language_tag")
        private val I18N_BUNDLE_LANGUAGE_TAG_KEY = stringPreferencesKey("i18n_bundle_language_tag")
        private val I18N_BUNDLE_JSON_KEY = stringPreferencesKey("i18n_bundle_json")
        private val SERVER_PORT_KEY = intPreferencesKey("server_port")
    }
}

data class ServerEndpointConfig(
    val host: String,
    val port: Int,
    val scheme: String
)
