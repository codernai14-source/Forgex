package com.forgex.mobile.core.network.exception

import com.forgex.mobile.core.common.i18n.AppText
import com.forgex.mobile.core.common.result.AppResult
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 统一错误码映射器。
 */
class ErrorCodeMapper {

    fun fromCode(code: Int?, message: String): AppResult.Error {
        return when (code) {
            401 -> mapped(code, message, "network.error.loginExpired", "登录已失效")
            601 -> mapped(code, message, "network.error.unauthorized", "无权限")
            602 -> mapped(code, message, "network.error.tenantRequired", "租户信息缺失或未选择")
            603 -> mapped(code, message, "network.error.moduleOffline", "服务不可用，请稍后重试")
            604 -> mapped(code, message, "network.error.authorizationRequired", "需要授权")
            605 -> mapped(code, message, "network.error.authorizationInvalid", "授权无效")
            606 -> mapped(code, message, "network.error.passwordExpired", "密码已过期，请修改密码")
            9001 -> mapped(code, message, "network.error.licenseInvalid", "License 已失效")
            else -> AppResult.Error(message = message.ifBlank { "请求失败" }, code = code)
        }
    }

    fun fromThrowable(throwable: Throwable): AppResult.Error {
        return when (throwable) {
            is SocketTimeoutException -> AppResult.Error(message = "请求超时，请稍后重试")
            is IOException -> AppResult.Error(message = "网络连接异常，请检查网络后重试")
            else -> AppResult.Error(message = throwable.message ?: "未知错误")
        }
    }

    private fun mapped(
        code: Int,
        message: String,
        key: String,
        fallback: String
    ): AppResult.Error {
        return AppResult.Error(
            message = message.ifBlank { fallback },
            code = code,
            appText = AppText.Dynamic(key = key, fallbackRaw = fallback)
        )
    }
}
