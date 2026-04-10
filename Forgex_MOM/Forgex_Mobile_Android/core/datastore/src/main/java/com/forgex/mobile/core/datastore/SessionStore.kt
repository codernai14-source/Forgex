package com.forgex.mobile.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
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

    val token: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[TOKEN_KEY] }

    val tenantId: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[TENANT_ID_KEY] }

    val account: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[ACCOUNT_KEY] }

    val systemName: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[SYSTEM_NAME_KEY] }

    val serverHost: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[SERVER_HOST_KEY] }

    val serverPort: Flow<Int?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[SERVER_PORT_KEY] }

    val serverScheme: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences -> preferences[SERVER_SCHEME_KEY] }

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

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(TENANT_ID_KEY)
            preferences.remove(ACCOUNT_KEY)
        }
    }

    companion object {
        private const val DATASTORE_NAME = "forgex_mobile_session.preferences_pb"
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val TENANT_ID_KEY = stringPreferencesKey("tenant_id")
        private val ACCOUNT_KEY = stringPreferencesKey("account")
        private val SYSTEM_NAME_KEY = stringPreferencesKey("system_name")
        private val SERVER_HOST_KEY = stringPreferencesKey("server_host")
        private val SERVER_SCHEME_KEY = stringPreferencesKey("server_scheme")
        private val SERVER_PORT_KEY = intPreferencesKey("server_port")
    }
}

data class ServerEndpointConfig(
    val host: String,
    val port: Int,
    val scheme: String
)
