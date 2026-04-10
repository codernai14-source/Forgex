package com.forgex.mobile.feature.auth.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.SystemBasicConfig
import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.core.network.model.menu.UserRoutesVO

/**
 * 登录模块数据仓库接口。
 */
interface AuthRepository {

    suspend fun login(
        account: String,
        password: String,
        captcha: String? = null,
        captchaId: String? = null,
        publicKey: String? = null
    ): AppResult<List<TenantVO>>

    suspend fun loadCaptchaMode(): AppResult<CaptchaMode>

    suspend fun loadSystemBasicConfig(): AppResult<SystemBasicConfig>

    suspend fun loadImageCaptcha(): AppResult<ImageCaptcha>

    suspend fun loadSliderCaptcha(): AppResult<SliderCaptcha>

    suspend fun validateSliderCaptcha(
        captchaId: String,
        left: Float,
        bgImageWidth: Int,
        bgImageHeight: Int,
        templateImageWidth: Int,
        templateImageHeight: Int
    ): AppResult<String>

    suspend fun loadPublicKey(): AppResult<String>

    suspend fun chooseTenant(
        tenantId: String,
        account: String
    ): AppResult<SysUserDTO>

    suspend fun loadUserRoutes(account: String): AppResult<UserRoutesVO>

    suspend fun logout(): AppResult<Boolean>
}
