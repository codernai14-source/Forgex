package com.forgex.mobile.core.network.model.auth

data class SysUserDTO(
    val id: Long,
    val account: String,
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val status: Boolean? = null,
    val tenantId: Long? = null
)
