package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.auth.LoginCaptchaConfig
import com.forgex.mobile.core.network.model.auth.LoginRequest
import com.forgex.mobile.core.network.model.auth.SliderCaptchaPayload
import com.forgex.mobile.core.network.model.auth.SliderValidateRequest
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.TenantChoiceRequest
import com.forgex.mobile.core.network.model.auth.TenantVO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @GET("auth/crypto/public-key")
    suspend fun getPublicKey(): ApiResponse<String>

    @GET("sys/config/login-captcha")
    suspend fun getLoginCaptchaConfig(): ApiResponse<LoginCaptchaConfig>

    @POST("auth/captcha/image")
    suspend fun getImageCaptcha(): ApiResponse<Map<String, String>>

    @POST("auth/captcha/slider")
    suspend fun getSliderCaptcha(): ApiResponse<SliderCaptchaPayload>

    @POST("auth/captcha/slider/validate")
    suspend fun validateSliderCaptcha(@Body request: SliderValidateRequest): ApiResponse<String>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<List<TenantVO>>

    @POST("auth/choose-tenant")
    suspend fun chooseTenant(@Body request: TenantChoiceRequest): ApiResponse<SysUserDTO>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Boolean>
}
