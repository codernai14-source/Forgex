package com.forgex.mobile.core.network.model.auth

data class TenantChoiceRequest(
    val loginTerminal: String = "C",
    val tenantId: String,
    val account: String,
    val interactionCode: String? = null,
    val username: String? = null
)
