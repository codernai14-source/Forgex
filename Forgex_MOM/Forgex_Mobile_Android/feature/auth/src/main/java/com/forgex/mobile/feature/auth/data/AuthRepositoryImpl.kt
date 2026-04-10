package com.forgex.mobile.feature.auth.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.api.AuthApi
import com.forgex.mobile.core.network.api.MenuApi
import com.forgex.mobile.core.network.model.auth.LoginRequest
import com.forgex.mobile.core.network.model.auth.SysUserDTO
import com.forgex.mobile.core.network.model.auth.TenantChoiceRequest
import com.forgex.mobile.core.network.model.auth.TenantVO
import com.forgex.mobile.core.network.model.menu.RoutesRequest
import com.forgex.mobile.core.network.model.menu.UserRoutesVO
import javax.inject.Inject
import javax.inject.Singleton

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
        captchaId: String?
    ): AppResult<List<TenantVO>> {
        return try {
            val response = authApi.login(
                LoginRequest(
                    account = account,
                    password = password,
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
}
