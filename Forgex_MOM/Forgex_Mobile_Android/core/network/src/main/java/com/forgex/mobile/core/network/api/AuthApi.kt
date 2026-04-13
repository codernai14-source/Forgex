package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.auth.LoginCaptchaConfig
import com.forgex.mobile.core.network.model.auth.LoginRequest
import com.forgex.mobile.core.network.model.auth.SliderCaptchaPayload
import com.forgex.mobile.core.network.model.auth.SliderValidateRequest
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.SystemBasicConfig
import com.forgex.mobile.core.network.model.auth.TenantChoiceRequest
import com.forgex.mobile.core.network.model.auth.TenantVO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    /** 获取登录加密公钥（SM2） */
    @GET("auth/crypto/public-key")
    suspend fun getPublicKey(): ApiResponse<String>

    /** 获取登录验证码模式（none/image/slider） */
    @GET("sys/config/login-captcha")
    suspend fun getLoginCaptchaConfig(): ApiResponse<LoginCaptchaConfig>

    /** 获取系统基础配置（登录标题、Logo、背景等） */
    @GET("sys/config/system-basic")
    suspend fun getSystemBasicConfig(): ApiResponse<SystemBasicConfig>

    /** 获取图片验证码 */
    @POST("auth/captcha/image")
    suspend fun getImageCaptcha(): ApiResponse<Map<String, Any?>>

    /** 获取滑块验证码 */
    @POST("auth/captcha/slider")
    suspend fun getSliderCaptcha(): ApiResponse<SliderCaptchaPayload>

    /** 校验滑块验证码 */
    @POST("auth/captcha/slider/validate")
    suspend fun validateSliderCaptcha(@Body request: SliderValidateRequest): ApiResponse<String>

    /** 账号密码登录 */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<List<TenantVO>>

    /** 登录后选租户 */
    @POST("auth/chooseTenant")
    suspend fun chooseTenant(@Body request: TenantChoiceRequest): ApiResponse<SysUserDTO>

    /** 退出登录 */
    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Boolean>
}
