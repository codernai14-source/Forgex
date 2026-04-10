package com.forgex.mobile.feature.auth

import com.forgex.mobile.core.network.model.auth.TenantVO

enum class AuthStep {
    LOGIN,
    TENANT_SELECTION
}

data class AuthUiState(
    val account: String = "",
    val password: String = "",
    val captcha: String = "",
    val captchaId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val step: AuthStep = AuthStep.LOGIN,
    val tenants: List<TenantVO> = emptyList(),
    val selectedTenantId: String? = null
)

sealed interface AuthEvent {
    data object LoginCompleted : AuthEvent
}
