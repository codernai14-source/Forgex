package com.forgex.mobile.core.network.model.auth

data class LoginRequest(
    val loginTerminal: String = "C",
    val loginType: String = "ACCOUNT_PASSWORD",
    val account: String,
    val password: String,
    val captcha: String? = null,
    val captchaId: String? = null
)
