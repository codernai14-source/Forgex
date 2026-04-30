package com.forgex.mobile.core.network.repository_support

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.exception.ErrorCodeMapper

/**
 * 统一网络请求执行模板。
 */
class ApiExecutor(
    private val errorCodeMapper: ErrorCodeMapper = ErrorCodeMapper()
) {

    suspend fun <T> request(block: suspend () -> ApiResponse<T>): AppResult<T> {
        return try {
            val response = block()
            if (response.isSuccess()) {
                AppResult.Success(response.data as T)
            } else {
                errorCodeMapper.fromCode(response.code, response.errorMessage())
            }
        } catch (throwable: Throwable) {
            errorCodeMapper.fromThrowable(throwable)
        }
    }

    suspend fun <T, R> request(block: suspend () -> ApiResponse<T>, mapper: (T?) -> R): AppResult<R> {
        return try {
            val response = block()
            if (response.isSuccess()) {
                AppResult.Success(mapper(response.data))
            } else {
                errorCodeMapper.fromCode(response.code, response.errorMessage())
            }
        } catch (throwable: Throwable) {
            errorCodeMapper.fromThrowable(throwable)
        }
    }
}
