package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.auth.LoginRequest
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.TenantChoiceRequest
import com.forgex.mobile.core.network.model.auth.TenantVO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @GET("auth/crypto/public-key")
    suspend fun getPublicKey(): ApiResponse<String>

    @POST("auth/captcha/image")
    suspend fun getImageCaptcha(): ApiResponse<Map<String, String>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<List<TenantVO>>

    @POST("auth/choose-tenant")
    suspend fun chooseTenant(@Body request: TenantChoiceRequest): ApiResponse<SysUserDTO>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Boolean>
}
