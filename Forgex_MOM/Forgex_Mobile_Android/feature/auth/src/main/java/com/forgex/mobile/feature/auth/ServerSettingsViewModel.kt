package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.datastore.ServerEndpointConfig
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.di.BaseUrl
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

/**
 * 服务器配置页状态。
 */
data class ServerSettingsUiState(
    val scheme: String = "http",
    val host: String = "",
    val port: String = "9000",
    val defaultEndpoint: String = "http://10.0.2.2:9000",
    val currentEndpoint: String = "http://10.0.2.2:9000",
    val usingCustomEndpoint: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

sealed interface ServerSettingsEvent {
    data object Saved : ServerSettingsEvent
}

/**
 * 登录前服务器环境切换：
 * 1. 保存自定义网关协议/IP/端口。
 * 2. 一键恢复默认网关。
 * 3. 切换后清理登录态，避免跨环境会话污染。
 */
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
        // 页面打开时实时反映当前生效的 endpoint（默认/自定义）
        observeCustomEndpoint()
    }

    fun updateHost(value: String) {
        _uiState.update {
            it.copy(
                host = value.trim(),
                errorMessage = null,
                message = null
            )
        }
    }

    fun updatePort(value: String) {
        _uiState.update {
            it.copy(
                port = value.trim(),
                errorMessage = null,
                message = null
            )
        }
    }

    fun updateScheme(value: String) {
        _uiState.update {
            it.copy(
                scheme = value,
                errorMessage = null,
                message = null
            )
        }
    }

    fun saveEndpoint() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val normalizedHost = normalizeHost(snapshot.host)
            if (normalizedHost.isBlank()) {
                _uiState.update { it.copy(errorMessage = "请输入服务器 IP 或域名") }
                return@launch
            }
            val portValue = snapshot.port.toIntOrNull()
            if (portValue == null || portValue !in 1..65535) {
                _uiState.update { it.copy(errorMessage = "端口必须是 1 到 65535 之间的数字") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null, message = null) }
            sessionStore.saveServerEndpoint(
                host = normalizedHost,
                port = portValue,
                scheme = snapshot.scheme
            )
            // 环境切换后旧 token/cookie 失效概率高，主动清会话保证一致性
            sessionStore.clearSession()
            _uiState.update {
                it.copy(
                    isSaving = false,
                    message = "服务器已切换，需重新登录",
                    errorMessage = null
                )
            }
            _events.emit(ServerSettingsEvent.Saved)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, message = null) }
            sessionStore.clearServerEndpoint()
            // 恢复默认环境同样清会话，避免误用旧环境登录态
            sessionStore.clearSession()
            _uiState.update {
                it.copy(
                    scheme = defaultEndpoint.scheme,
                    host = defaultEndpoint.host,
                    port = defaultEndpoint.port.toString(),
                    isSaving = false,
                    message = "已恢复默认服务器，需重新登录",
                    errorMessage = null
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

    /**
     * 兼容用户输入：
     * - `http://10.0.2.2:9000/api` -> `10.0.2.2`
     * - `my.domain.com/path` -> `my.domain.com`
     */
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
