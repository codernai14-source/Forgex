package com.forgex.mobile.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
    }
}
