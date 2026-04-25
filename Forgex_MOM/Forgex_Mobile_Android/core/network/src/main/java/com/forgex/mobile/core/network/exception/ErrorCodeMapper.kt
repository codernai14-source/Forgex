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
            401 -> AppResult.Error(message = message.ifBlank { "登录已失效" }, code = code)
            602 -> AppResult.Error(message = message.ifBlank { "租户信息缺失或未选择" }, code = code)
            9001 -> AppResult.Error(message = message.ifBlank { "License 已失效" }, code = code)
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
}
