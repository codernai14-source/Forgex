package com.forgex.mobile.feature.auth

import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.feature.auth.data.CaptchaMode
import com.forgex.mobile.feature.auth.data.SliderCaptcha

/** 登录页面步骤：先登录，再选租户。 */
enum class AuthStep {
    LOGIN,
    TENANT_SELECTION
}

enum class AuthLoginStage {
    ENTRY,
    PASSWORD_FORM
}

/** 登录方式枚举：除账号登录外其余暂为占位。 */
enum class LoginMethod {
    ACCOUNT_PASSWORD,
    WECHAT,
    DING_TALK,
    FEI_SHU
}

/** 登录页状态模型。 */
data class AuthUiState(
    val systemName: String = "FORGEX_MOM",
    val loginTitle: String = "欢迎使用 FORGEX",
    val loginSubtitle: String = "",
    val systemLogo: String = "",
    val loginBackgroundType: String = "image",
    val loginBackgroundImage: String = "",
    val loginBackgroundColor: String = "#0B1E48",
    val showOAuthLogin: Boolean = true,
    val showRegisterEntry: Boolean = true,
    val registerUrl: String = "/register",
    val primaryColor: String = "#05d9e8",
    val secondaryColor: String = "#ff2a6d",
    val serverOrigin: String = "",
    val loginStage: AuthLoginStage = AuthLoginStage.ENTRY,
    val selectedLoginMethod: LoginMethod = LoginMethod.ACCOUNT_PASSWORD,
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
