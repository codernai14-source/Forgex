package com.forgex.mobile.core.network.model.auth

data class TenantVO(
    val id: String,
    val name: String,
    val logo: String? = null,
    val intro: String? = null,
    val isDefault: Boolean? = null,
    val tenantType: String? = null
)
