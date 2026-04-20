package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.datastore.ServerEndpointConfig
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.di.BaseUrl
import com.forgex.mobile.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServerSettingsUiState(
    val scheme: String = "http",
    val host: String = "",
    val port: String = "9000",
    val defaultEndpoint: String = "http://10.0.2.2:9000",
    val currentEndpoint: String = "http://10.0.2.2:9000",
    val usingCustomEndpoint: Boolean = false,
    val isSaving: Boolean = false,
    val messageRes: Int? = null,
    val errorMessageRes: Int? = null
)

sealed interface ServerSettingsEvent {
    data object Saved : ServerSettingsEvent
}

@HiltViewModel
class ServerSettingsViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    @BaseUrl private val baseUrl: String
) : ViewModel() {

    private val defaultEndpoint = parseDefaultEndpoint(baseUrl)

    private val _uiState = MutableStateFlow(
        ServerSettingsUiState(
            scheme = defaultEndpoint.scheme,
            host = defaultEndpoint.host,
            port = defaultEndpoint.port.toString(),
            defaultEndpoint = endpointToString(defaultEndpoint),
            currentEndpoint = endpointToString(defaultEndpoint)
        )
    )
    val uiState: StateFlow<ServerSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ServerSettingsEvent>()
    val events: SharedFlow<ServerSettingsEvent> = _events.asSharedFlow()

    init {
        observeCustomEndpoint()
    }

    fun updateHost(value: String) {
        _uiState.update {
            it.copy(
                host = value.trim(),
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    fun updatePort(value: String) {
        _uiState.update {
            it.copy(
                port = value.trim(),
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    fun updateScheme(value: String) {
        _uiState.update {
            it.copy(
                scheme = value,
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    fun saveEndpoint() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val normalizedHost = normalizeHost(snapshot.host)
            if (normalizedHost.isBlank()) {
                _uiState.update { it.copy(errorMessageRes = R.string.server_settings_host_required) }
                return@launch
            }
            val portValue = snapshot.port.toIntOrNull()
            if (portValue == null || portValue !in 1..65535) {
                _uiState.update { it.copy(errorMessageRes = R.string.server_settings_port_invalid) }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessageRes = null, messageRes = null) }
            sessionStore.saveServerEndpoint(
                host = normalizedHost,
                port = portValue,
                scheme = snapshot.scheme
            )
            sessionStore.clearSession()
            _uiState.update {
                it.copy(
                    isSaving = false,
                    messageRes = R.string.server_settings_saved,
                    errorMessageRes = null
                )
            }
            _events.emit(ServerSettingsEvent.Saved)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessageRes = null, messageRes = null) }
            sessionStore.clearServerEndpoint()
            sessionStore.clearSession()
            _uiState.update {
                it.copy(
                    scheme = defaultEndpoint.scheme,
                    host = defaultEndpoint.host,
                    port = defaultEndpoint.port.toString(),
                    isSaving = false,
                    messageRes = R.string.server_settings_reset_done,
                    errorMessageRes = null
                )
            }
            _events.emit(ServerSettingsEvent.Saved)
        }
    }

    private fun observeCustomEndpoint() {
        viewModelScope.launch {
            sessionStore.serverEndpoint.collectLatest { endpoint ->
                val activeEndpoint = endpoint ?: defaultEndpoint
                _uiState.update {
                    it.copy(
                        scheme = activeEndpoint.scheme,
                        host = activeEndpoint.host,
                        port = activeEndpoint.port.toString(),
                        usingCustomEndpoint = endpoint != null,
                        currentEndpoint = endpointToString(activeEndpoint)
                    )
                }
            }
        }
    }

    private fun parseDefaultEndpoint(baseUrlValue: String): ServerEndpointConfig {
        return runCatching {
            val uri = URI(baseUrlValue)
            val scheme = uri.scheme?.ifBlank { "http" } ?: "http"
            val host = uri.host?.ifBlank { "10.0.2.2" } ?: "10.0.2.2"
            val port = if (uri.port > 0) {
                uri.port
            } else if (scheme.equals("https", ignoreCase = true)) {
                443
            } else {
                80
            }
            ServerEndpointConfig(host = host, port = port, scheme = scheme)
        }.getOrElse {
            ServerEndpointConfig(host = "10.0.2.2", port = 9000, scheme = "http")
        }
    }

    private fun endpointToString(endpoint: ServerEndpointConfig): String {
        return "${endpoint.scheme}://${endpoint.host}:${endpoint.port}"
    }

    private fun normalizeHost(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        var host = trimmed
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
        if (host.contains(":")) {
            host = host.substringBefore(":")
        }
        return host.trim()
    }
}
