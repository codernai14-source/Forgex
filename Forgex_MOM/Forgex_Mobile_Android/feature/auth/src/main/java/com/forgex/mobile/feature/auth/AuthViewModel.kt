package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.feature.auth.data.AuthRepository
import com.forgex.mobile.feature.auth.data.CaptchaMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
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

    fun refreshCaptcha(silent: Boolean = false) {
        when (_uiState.value.captchaMode) {
            CaptchaMode.IMAGE -> refreshImageCaptcha(silent)
            CaptchaMode.SLIDER -> refreshSliderCaptcha(silent)
            CaptchaMode.NONE -> refreshCaptchaModeAndData(silent)
        }
    }

    private fun refreshCaptchaModeAndData(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

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
                            captchaMode = CaptchaMode.NONE,
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
                    // 预加载菜单/按钮权限；失败不阻断进入首页
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
}
