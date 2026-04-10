package com.forgex.mobile.feature.auth

import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.feature.auth.data.CaptchaMode
import com.forgex.mobile.feature.auth.data.SliderCaptcha

enum class AuthStep {
    LOGIN,
    TENANT_SELECTION
}

data class AuthUiState(
    val account: String = "",
    val password: String = "",
    val captchaMode: CaptchaMode = CaptchaMode.NONE,
    val captcha: String = "",
    val captchaId: String = "",
    val captchaImageBase64: String = "",
    val sliderCaptcha: SliderCaptcha? = null,
    val sliderProgress: Float = 0f,
    val sliderToken: String = "",
    val publicKeyLoaded: Boolean = false,
    val publicKey: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val step: AuthStep = AuthStep.LOGIN,
    val tenants: List<TenantVO> = emptyList(),
    val selectedTenantId: String? = null
)

sealed interface AuthEvent {
    data object LoginCompleted : AuthEvent
}
