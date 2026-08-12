package com.forgex.mobile.core.network.model.auth

/**
 * 账号凭据校验成功后的短期登录结果。
 */
data class LoginResult(
    val interactionCode: String,
    val tenants: List<TenantVO> = emptyList()
)
