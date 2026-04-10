package com.forgex.mobile.feature.auth.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.api.AuthApi
import com.forgex.mobile.core.network.api.MenuApi
import com.forgex.mobile.core.network.model.auth.LoginRequest
import com.forgex.mobile.core.network.model.auth.SliderTrackPayload
import com.forgex.mobile.core.network.model.auth.SliderTrackPointPayload
import com.forgex.mobile.core.network.model.auth.SliderValidateRequest
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.SystemBasicConfig
import com.forgex.mobile.core.network.model.auth.TenantChoiceRequest
import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.core.network.model.menu.RoutesRequest
import com.forgex.mobile.core.network.model.menu.UserRoutesVO
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val menuApi: MenuApi,
    private val sessionStore: SessionStore
) : AuthRepository {

    override suspend fun login(
        account: String,
        password: String,
        captcha: String?,
        captchaId: String?,
        publicKey: String?
    ): AppResult<List<TenantVO>> {
        return try {
            val payloadPassword = if (publicKey.isNullOrBlank()) {
                password
            } else {
                Sm2Encryptor.encryptToHex(password, publicKey)
                    ?: return AppResult.Error("密码加密失败，请刷新后重试")
            }

            val response = authApi.login(
                LoginRequest(
                    account = account,
                    password = payloadPassword,
                    captcha = captcha?.takeIf { it.isNotBlank() },
                    captchaId = captchaId?.takeIf { it.isNotBlank() }
                )
            )

            if (response.isSuccess()) {
                AppResult.Success(response.data ?: emptyList())
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Login request failed")
        }
    }

    override suspend fun loadCaptchaMode(): AppResult<CaptchaMode> {
        return try {
            val response = authApi.getLoginCaptchaConfig()
            if (response.isSuccess()) {
                val mode = when (response.data?.mode?.trim()?.lowercase()) {
                    "image" -> CaptchaMode.IMAGE
                    "slider" -> CaptchaMode.SLIDER
                    else -> CaptchaMode.NONE
                }
                AppResult.Success(mode)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load captcha mode failed")
        }
    }

    override suspend fun loadSystemBasicConfig(): AppResult<SystemBasicConfig> {
        return try {
            val response = authApi.getSystemBasicConfig()
            val config = response.data
            if (response.isSuccess() && config != null) {
                AppResult.Success(config)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load system config failed")
        }
    }

    override suspend fun loadImageCaptcha(): AppResult<ImageCaptcha> {
        return try {
            val response = authApi.getImageCaptcha()
            val data = response.data
            // 兼容后端可能的字段差异，减少“验证码不显示”问题
            val captchaId = extractString(data, "captchaId", "id")
            val imageBase64 = extractString(
                data,
                "imageBase64",
                "captchaImage",
                "image",
                "base64"
            )

            if (response.isSuccess() && captchaId.isNotBlank() && imageBase64.isNotBlank()) {
                AppResult.Success(
                    ImageCaptcha(
                        captchaId = captchaId,
                        imageBase64 = imageBase64
                    )
                )
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load image captcha failed")
        }
    }

    override suspend fun loadSliderCaptcha(): AppResult<SliderCaptcha> {
        return try {
            val response = authApi.getSliderCaptcha()
            val data = response.data
            val captchaId = data?.id.orEmpty()
            val backgroundImage = data?.backgroundImage.orEmpty()
            val templateImage = data?.templateImage.orEmpty()
            val bgWidth = data?.backgroundImageWidth ?: 0
            val bgHeight = data?.backgroundImageHeight ?: 0
            val templateWidth = data?.templateImageWidth ?: 0
            val templateHeight = data?.templateImageHeight ?: 0

            if (
                response.isSuccess() &&
                captchaId.isNotBlank() &&
                backgroundImage.isNotBlank() &&
                templateImage.isNotBlank() &&
                bgWidth > 0 &&
                bgHeight > 0 &&
                templateWidth > 0 &&
                templateHeight > 0
            ) {
                AppResult.Success(
                    SliderCaptcha(
                        id = captchaId,
                        backgroundImageBase64 = backgroundImage,
                        templateImageBase64 = templateImage,
                        backgroundImageWidth = bgWidth,
                        backgroundImageHeight = bgHeight,
                        templateImageWidth = templateWidth,
                        templateImageHeight = templateHeight
                    )
                )
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load slider captcha failed")
        }
    }

    override suspend fun validateSliderCaptcha(
        captchaId: String,
        left: Float,
        bgImageWidth: Int,
        bgImageHeight: Int,
        templateImageWidth: Int,
        templateImageHeight: Int
    ): AppResult<String> {
        return try {
            val normalizedLeft = left.coerceIn(0f, (bgImageWidth - templateImageWidth).coerceAtLeast(0).toFloat())
            val track = SliderTrackPayload(
                bgImageWidth = bgImageWidth,
                bgImageHeight = bgImageHeight,
                templateImageWidth = templateImageWidth,
                templateImageHeight = templateImageHeight,
                startTime = System.currentTimeMillis() - 700L,
                stopTime = System.currentTimeMillis(),
                left = normalizedLeft.roundToInt(),
                top = 0,
                trackList = listOf(
                    SliderTrackPointPayload(
                        x = 0f,
                        y = 0f,
                        t = 0f,
                        type = "DOWN"
                    ),
                    SliderTrackPointPayload(
                        x = normalizedLeft * 0.45f,
                        y = 0f,
                        t = 260f,
                        type = "MOVE"
                    ),
                    SliderTrackPointPayload(
                        x = normalizedLeft,
                        y = 0f,
                        t = 640f,
                        type = "UP"
                    )
                )
            )
            val response = authApi.validateSliderCaptcha(
                SliderValidateRequest(
                    id = captchaId,
                    track = track
                )
            )
            val token = response.data?.takeIf { it.isNotBlank() }
            if (response.isSuccess() && token != null) {
                AppResult.Success(token)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Validate slider captcha failed")
        }
    }

    override suspend fun loadPublicKey(): AppResult<String> {
        return try {
            val response = authApi.getPublicKey()
            val publicKey = response.data?.takeIf { it.isNotBlank() }
            if (response.isSuccess() && publicKey != null) {
                AppResult.Success(publicKey)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load public key failed")
        }
    }

    override suspend fun chooseTenant(tenantId: String, account: String): AppResult<SysUserDTO> {
        return try {
            val response = authApi.chooseTenant(
                TenantChoiceRequest(
                    tenantId = tenantId,
                    account = account
                )
            )

            val user = response.data
            if (response.isSuccess() && user != null) {
                sessionStore.saveSession(
                    account = user.account.ifBlank { account },
                    tenantId = user.tenantId?.toString() ?: tenantId
                )
                AppResult.Success(user)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Choose tenant request failed")
        }
    }

    override suspend fun loadUserRoutes(account: String): AppResult<UserRoutesVO> {
        return try {
            val response = menuApi.getRoutes(RoutesRequest(account))
            if (response.isSuccess()) {
                AppResult.Success(response.data ?: UserRoutesVO())
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load routes request failed")
        }
    }

    override suspend fun logout(): AppResult<Boolean> {
        return try {
            val response = authApi.logout()
            sessionStore.clearSession()
            if (response.isSuccess()) {
                AppResult.Success(response.data ?: true)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            sessionStore.clearSession()
            AppResult.Error(e.message ?: "Logout request failed")
        }
    }

    private fun extractString(
        data: Map<String, Any?>?,
        vararg keys: String
    ): String {
        if (data == null) return ""
        // 兜底清理空白符，避免 Base64 中换行导致图片解码失败
        val value = keys
            .firstNotNullOfOrNull { key -> data[key]?.toString()?.trim() }
            .orEmpty()
        return value.replace("\\s".toRegex(), "")
    }
}
