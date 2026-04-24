package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.datastore.ServerEndpointConfig
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.model.FxScanResult
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

/**
 * 服务器配置页状态管理，负责维护地址表单与扫描回填结果。
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
        observeCustomEndpoint()
    }

    /**
     * 更新服务器主机输入。
     */
    fun updateHost(value: String) {
        _uiState.update {
            it.copy(
                host = value.trim(),
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    /**
     * 更新端口输入。
     */
    fun updatePort(value: String) {
        _uiState.update {
            it.copy(
                port = value.trim(),
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    /**
     * 更新访问协议。
     */
    fun updateScheme(value: String) {
        _uiState.update {
            it.copy(
                scheme = value,
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    /**
     * 应用扫描结果到主机输入框。
     */
    fun applyHostScan(result: FxScanResult) {
        val normalized = normalizeHost(result.rawValue)
        if (normalized.isBlank()) {
            return
        }
        _uiState.update {
            it.copy(
                host = normalized,
                latestHostScanResult = result,
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    /**
     * 应用扫描结果到端口输入框。
     */
    fun applyPortScan(result: FxScanResult) {
        val normalizedPort = normalizePort(result.rawValue)
        if (normalizedPort.isBlank()) {
            return
        }
        _uiState.update {
            it.copy(
                port = normalizedPort,
                latestPortScanResult = result,
                errorMessageRes = null,
                messageRes = null
            )
        }
    }

    /**
     * 标记主机扫描结果已被页面消费。
     */
    fun consumeHostScanResult() {
        _uiState.update { it.copy(latestHostScanResult = null) }
    }

    /**
     * 标记端口扫描结果已被页面消费。
     */
    fun consumePortScanResult() {
        _uiState.update { it.copy(latestPortScanResult = null) }
    }

    /**
     * 保存当前服务器地址。
     */
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

    /**
     * 恢复默认服务器地址。
     */
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

    /**
     * 同步当前有效服务器地址。
     */
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

    /**
     * 解析默认 BaseUrl 为标准端点对象。
     */
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

    /**
     * 将端点对象格式化为完整地址字符串。
     */
    private fun endpointToString(endpoint: ServerEndpointConfig): String {
        return "${endpoint.scheme}://${endpoint.host}:${endpoint.port}"
    }

    /**
     * 规范化主机字符串，去除协议、路径和端口残留。
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

    /**
     * 规范化端口字符串，只保留数字。
     */
    private fun normalizePort(raw: String): String {
        return raw.filter(Char::isDigit)
    }
}

/**
 * 服务器配置页 UI 状态。
 */
data class ServerSettingsUiState(
    val scheme: String = "http",
    val host: String = "",
    val port: String = "9000",
    val defaultEndpoint: String = "http://10.0.2.2:9000",
    val currentEndpoint: String = "http://10.0.2.2:9000",
    val usingCustomEndpoint: Boolean = false,
    val isSaving: Boolean = false,
    val messageRes: Int? = null,
    val errorMessageRes: Int? = null,
    val latestHostScanResult: FxScanResult? = null,
    val latestPortScanResult: FxScanResult? = null
)

/**
 * 服务器配置页单次事件。
 */
sealed interface ServerSettingsEvent {
    data object Saved : ServerSettingsEvent
}
