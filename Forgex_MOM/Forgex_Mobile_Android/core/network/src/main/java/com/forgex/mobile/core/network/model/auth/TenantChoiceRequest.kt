package com.forgex.mobile.core.network.model.auth

data class TenantChoiceRequest(
    val tenantId: String,
    val account: String,
    val username: String? = null
)
