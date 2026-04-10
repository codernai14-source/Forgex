package com.forgex.mobile.core.network.model.auth

data class LoginRequest(
    val account: String,
    val password: String,
    val captcha: String? = null,
    val captchaId: String? = null
)
