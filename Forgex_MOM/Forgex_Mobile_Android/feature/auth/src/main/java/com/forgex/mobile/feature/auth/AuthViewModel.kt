package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.ServerEndpointConfig
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.di.BaseUrl
import com.forgex.mobile.core.network.model.auth.SystemBasicConfig
import com.forgex.mobile.feature.auth.data.AuthRepository
import com.forgex.mobile.feature.auth.data.CaptchaMode
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
 * 登录页状态管理：
 * 1. 拉取系统配置（标题/Logo/背景）并同步到 UI。
 * 2. 管理验证码模式（无/图片/滑块）与登录提交校验。
 * 3. 监听动态服务器地址，实时展示当前登录目标环境。
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    @BaseUrl private val baseUrl: String
) : ViewModel() {

    private val defaultServerOrigin: String = resolveDefaultOrigin(baseUrl)

    private val _uiState = MutableStateFlow(AuthUiState(serverOrigin = defaultServerOrigin))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        // 启动时并行完成：环境地址监听、系统配置预热、验证码模式预热、公钥预热
        observeServerEndpoint()
        preloadSystemBasicConfig()
        refreshCaptchaModeAndData(silent = true)
        preloadPublicKey()
    }

    fun updateAccount(value: String) {
        _uiState.update { it.copy(account = value.trim(), errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun updateCaptcha(value: String) {
        _uiState.update { it.copy(captcha = value.trim(), errorMessage = null) }
    }

    fun updateSliderProgress(value: Float) {
        val normalized = value.coerceIn(0f, 1f)
        _uiState.update {
            it.copy(
                sliderProgress = normalized,
                sliderToken = "",
                errorMessage = null
            )
        }
    }

    fun switchLoginMethod(method: LoginMethod) {
        // 当前仅账号密码链路可用，其它入口保留占位提示
        if (method == LoginMethod.ACCOUNT_PASSWORD) {
            _uiState.update {
                it.copy(
                    selectedLoginMethod = method,
                    errorMessage = null
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                selectedLoginMethod = method,
                errorMessage = "第三方登录正在开发中"
            )
        }
    }

    fun openPasswordLogin() {
        _uiState.update {
            it.copy(
                loginStage = AuthLoginStage.PASSWORD_FORM,
                selectedLoginMethod = LoginMethod.ACCOUNT_PASSWORD,
                errorMessage = null
            )
        }
    }

    fun backToLoginEntry() {
        _uiState.update {
            it.copy(
                loginStage = AuthLoginStage.ENTRY,
                errorMessage = null
            )
        }
    }

    fun refreshCaptcha(silent: Boolean = false) {
        when (_uiState.value.captchaMode) {
            CaptchaMode.IMAGE -> refreshImageCaptcha(silent)
            CaptchaMode.SLIDER -> refreshSliderCaptcha(silent)
            CaptchaMode.NONE -> refreshCaptchaModeAndData(silent)
        }
    }

    private fun observeServerEndpoint() {
        viewModelScope.launch {
            // 当用户在“服务器配置页”修改地址后，这里会立即刷新登录页展示
            sessionStore.serverEndpoint.collectLatest { endpoint ->
                _uiState.update {
                    it.copy(serverOrigin = resolveServerOrigin(endpoint))
                }
            }
        }
    }

    private fun preloadSystemBasicConfig() {
        viewModelScope.launch {
            when (val result = authRepository.loadSystemBasicConfig()) {
                is AppResult.Success -> {
                    applySystemConfig(result.data)
                }

                is AppResult.Error -> {
                    // 拉取失败时至少把默认系统名写入会话，确保全局标题有值
                    sessionStore.saveSystemName(_uiState.value.systemName)
                }

                AppResult.Loading -> Unit
            }
        }
    }

    private suspend fun applySystemConfig(config: SystemBasicConfig) {
        // 统一兜底，避免后端空值导致 UI 抖动或显示异常
        val systemName = config.systemName.safeOrDefault("FORGEX_MOM")
        val loginTitle = config.loginPageTitle.safeOrDefault(systemName)
        val loginSubtitle = config.loginPageSubtitle.safeOrDefault("")
        val loginBackgroundType = config.loginBackgroundType.safeOrDefault("image")
        val loginBackgroundImage = config.loginBackgroundImage.safeOrDefault("")
        val loginBackgroundColor = config.loginBackgroundColor.safeOrDefault("#0B1E48")
        val showOAuthLogin = config.showOAuthLogin ?: true
        val showRegisterEntry = config.showRegisterEntry ?: true
        val registerUrl = config.registerUrl.safeOrDefault("/register")
        val primaryColor = config.primaryColor.safeOrDefault("#05d9e8")
        val secondaryColor = config.secondaryColor.safeOrDefault("#ff2a6d")

        sessionStore.saveSystemName(systemName)
        _uiState.update {
            it.copy(
                systemName = systemName,
                loginTitle = loginTitle,
                loginSubtitle = loginSubtitle,
                systemLogo = config.systemLogo.safeOrDefault(""),
                loginBackgroundType = loginBackgroundType,
                loginBackgroundImage = loginBackgroundImage,
                loginBackgroundColor = loginBackgroundColor,
                showOAuthLogin = showOAuthLogin,
                showRegisterEntry = showRegisterEntry,
                registerUrl = registerUrl,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
    }

    private fun refreshCaptchaModeAndData(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // 先确定模式，再拉取对应验证码数据，避免 UI 状态与后端配置错位
            when (val result = authRepository.loadCaptchaMode()) {
                is AppResult.Success -> {
                    when (result.data) {
                        CaptchaMode.IMAGE -> {
                            _uiState.update {
                                it.copy(
                                    captchaMode = CaptchaMode.IMAGE,
                                    sliderCaptcha = null,
                                    sliderProgress = 0f,
                                    sliderToken = "",
                                    captcha = "",
                                    captchaId = "",
                                    captchaImageBase64 = "",
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                            refreshImageCaptcha(silent = true)
                        }

                        CaptchaMode.SLIDER -> {
                            _uiState.update {
                                it.copy(
                                    captchaMode = CaptchaMode.SLIDER,
                                    captcha = "",
                                    captchaId = "",
                                    captchaImageBase64 = "",
                                    sliderProgress = 0f,
                                    sliderToken = "",
                                    sliderCaptcha = null,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                            refreshSliderCaptcha(silent = true)
                        }

                        CaptchaMode.NONE -> {
                            _uiState.update {
                                it.copy(
                                    captchaMode = CaptchaMode.NONE,
                                    captcha = "",
                                    captchaId = "",
                                    captchaImageBase64 = "",
                                    sliderProgress = 0f,
                                    sliderToken = "",
                                    sliderCaptcha = null,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        }
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.IMAGE,
                            isLoading = false,
                            errorMessage = if (silent) null else result.message
                        )
                    }
                    refreshImageCaptcha(silent = true)
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun refreshImageCaptcha(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            when (val result = authRepository.loadImageCaptcha()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.IMAGE,
                            captchaId = result.data.captchaId,
                            captchaImageBase64 = result.data.imageBase64,
                            captcha = "",
                            sliderCaptcha = null,
                            sliderProgress = 0f,
                            sliderToken = "",
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.IMAGE,
                            captchaId = "",
                            captchaImageBase64 = "",
                            captcha = "",
                            isLoading = false,
                            errorMessage = if (silent) null else result.message
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun refreshSliderCaptcha(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            when (val result = authRepository.loadSliderCaptcha()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.SLIDER,
                            sliderCaptcha = result.data,
                            sliderProgress = 0f,
                            sliderToken = "",
                            captcha = "",
                            captchaId = result.data.id,
                            captchaImageBase64 = "",
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            captchaMode = CaptchaMode.NONE,
                            sliderCaptcha = null,
                            sliderProgress = 0f,
                            sliderToken = "",
                            captcha = "",
                            captchaId = "",
                            isLoading = false,
                            errorMessage = if (silent) null else result.message
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun verifySliderCaptcha() {
        val snapshot = _uiState.value
        val slider = snapshot.sliderCaptcha
        if (snapshot.captchaMode != CaptchaMode.SLIDER || slider == null) {
            _uiState.update { it.copy(errorMessage = "当前不是滑块验证码模式") }
            return
        }

        val maxLeft = (slider.backgroundImageWidth - slider.templateImageWidth).coerceAtLeast(0)
        if (maxLeft <= 0) {
            _uiState.update { it.copy(errorMessage = "滑块参数异常，请刷新验证码") }
            return
        }

        val left = snapshot.sliderProgress * maxLeft.toFloat()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // 按后端需要传入滑轨参数，获得可用于登录提交的 sliderToken
            when (
                val result = authRepository.validateSliderCaptcha(
                    captchaId = slider.id,
                    left = left,
                    bgImageWidth = slider.backgroundImageWidth,
                    bgImageHeight = slider.backgroundImageHeight,
                    templateImageWidth = slider.templateImageWidth,
                    templateImageHeight = slider.templateImageHeight
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            sliderToken = result.data,
                            captchaId = slider.id,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            sliderToken = "",
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun preloadPublicKey() {
        viewModelScope.launch {
            when (val result = authRepository.loadPublicKey()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            publicKeyLoaded = true,
                            publicKey = result.data
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            publicKeyLoaded = false,
                            publicKey = ""
                        )
                    }
                }
            }
        }
    }

    fun selectTenant(tenantId: String) {
        _uiState.update { it.copy(selectedTenantId = tenantId, errorMessage = null) }
    }

    fun backToLogin() {
        _uiState.update {
            it.copy(
                step = AuthStep.LOGIN,
                tenants = emptyList(),
                selectedTenantId = null,
                errorMessage = null
            )
        }
    }

    fun submitLogin() {
        val snapshot = _uiState.value
        if (snapshot.selectedLoginMethod != LoginMethod.ACCOUNT_PASSWORD) {
            _uiState.update { it.copy(errorMessage = "第三方登录正在开发中") }
            return
        }
        if (snapshot.account.isBlank() || snapshot.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入账号和密码") }
            return
        }

        when (snapshot.captchaMode) {
            CaptchaMode.IMAGE -> {
                if (snapshot.captchaId.isBlank() || snapshot.captcha.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "请输入验证码") }
                    return
                }
            }

            CaptchaMode.SLIDER -> {
                if (snapshot.sliderToken.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "请先完成滑块验证") }
                    return
                }
            }

            CaptchaMode.NONE -> Unit
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 不同验证码模式对应不同提交字段
            val captcha = when (snapshot.captchaMode) {
                CaptchaMode.IMAGE -> snapshot.captcha
                CaptchaMode.SLIDER -> snapshot.sliderToken
                CaptchaMode.NONE -> null
            }
            val captchaId = when (snapshot.captchaMode) {
                CaptchaMode.IMAGE -> snapshot.captchaId
                else -> null
            }

            when (
                val result = authRepository.login(
                    account = snapshot.account,
                    password = snapshot.password,
                    captcha = captcha,
                    captchaId = captchaId,
                    publicKey = snapshot.publicKey.takeIf { it.isNotBlank() }
                )
            ) {
                is AppResult.Success -> {
                    val tenants = result.data
                    if (tenants.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "登录成功但未获取到租户，请联系管理员"
                            )
                        }
                    } else {
                        val defaultTenant = tenants.firstOrNull { it.isDefault == true } ?: tenants.first()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                step = AuthStep.TENANT_SELECTION,
                                tenants = tenants,
                                selectedTenantId = defaultTenant.id,
                                errorMessage = null
                            )
                        }
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    refreshCaptcha(silent = true)
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun confirmTenantSelection() {
        val snapshot = _uiState.value
        val tenantId = snapshot.selectedTenantId

        if (snapshot.account.isBlank()) {
            _uiState.update { it.copy(errorMessage = "账号为空，请返回重新登录") }
            return
        }
        if (tenantId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "请选择租户") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val chooseResult = authRepository.chooseTenant(tenantId, snapshot.account)) {
                is AppResult.Success -> {
                    // 选租户成功后预加载菜单，不阻塞登录完成事件
                    authRepository.loadUserRoutes(snapshot.account)
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.LoginCompleted)
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = chooseResult.message
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun resolveServerOrigin(endpoint: ServerEndpointConfig?): String {
        if (endpoint == null || endpoint.host.isBlank()) {
            return defaultServerOrigin
        }
        val scheme = endpoint.scheme.ifBlank { "http" }
        return "$scheme://${endpoint.host}:${endpoint.port}"
    }

    /**
     * 从 BASE_URL 解析默认网关展示地址。
     * 例：`http://10.0.2.2:9000/api/` -> `http://10.0.2.2:9000`
     */
    private fun resolveDefaultOrigin(defaultBaseUrl: String): String {
        return runCatching {
            val uri = URI(defaultBaseUrl)
            val scheme = uri.scheme?.ifBlank { "http" } ?: "http"
            val host = uri.host?.ifBlank { "10.0.2.2" } ?: "10.0.2.2"
            val port = if (uri.port > 0) {
                uri.port
            } else if (scheme.equals("https", ignoreCase = true)) {
                443
            } else {
                80
            }
            "$scheme://$host:$port"
        }.getOrElse { "http://10.0.2.2:9000" }
    }

    private fun String?.safeOrDefault(default: String): String {
        return this?.trim().takeUnless { it.isNullOrBlank() } ?: default
    }
}
