package com.forgex.mobile.feature.auth.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.core.network.model.menu.UserRoutesVO

interface AuthRepository {

    suspend fun login(
        account: String,
        password: String,
        captcha: String? = null,
        captchaId: String? = null
    ): AppResult<List<TenantVO>>

    suspend fun chooseTenant(
        tenantId: String,
        account: String
    ): AppResult<SysUserDTO>

    suspend fun loadUserRoutes(account: String): AppResult<UserRoutesVO>

    suspend fun logout(): AppResult<Boolean>
}
