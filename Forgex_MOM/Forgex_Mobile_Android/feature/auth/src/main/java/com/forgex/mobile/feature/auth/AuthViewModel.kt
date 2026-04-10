package com.forgex.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.feature.auth.data.AuthRepository
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

    fun updateAccount(value: String) {
        _uiState.update { it.copy(account = value.trim(), errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun updateCaptcha(value: String) {
        _uiState.update { it.copy(captcha = value.trim(), errorMessage = null) }
    }

    fun updateCaptchaId(value: String) {
        _uiState.update { it.copy(captchaId = value.trim(), errorMessage = null) }
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (
                val result = authRepository.login(
                    account = snapshot.account,
                    password = snapshot.password,
                    captcha = snapshot.captcha,
                    captchaId = snapshot.captchaId
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
