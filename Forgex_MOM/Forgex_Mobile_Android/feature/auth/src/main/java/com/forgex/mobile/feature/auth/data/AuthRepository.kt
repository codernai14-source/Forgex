package com.forgex.mobile.feature.auth.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.LoginResult
import com.forgex.mobile.core.network.model.auth.SystemBasicConfig
import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.core.network.model.workbench.CMenuBundleVO

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
    ): AppResult<LoginResult>

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
        account: String,
        interactionCode: String
    ): AppResult<SysUserDTO>

    /**
     * 选租户成功后预载 C 端菜单聚合包（授权模块整树 + 收藏）并写入本地缓存。
     */
    suspend fun preloadCMenuBundle(tenantId: String): AppResult<CMenuBundleVO>

    suspend fun logout(): AppResult<Boolean>
}
