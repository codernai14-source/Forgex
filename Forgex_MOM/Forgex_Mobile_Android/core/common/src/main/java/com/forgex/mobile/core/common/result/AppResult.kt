package com.forgex.mobile.core.common.result

import com.forgex.mobile.core.common.i18n.AppText

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>

    data class Error(
        val message: String,
        val code: Int? = null,
        val appText: AppText? = null
    ) : AppResult<Nothing>

    data object Loading : AppResult<Nothing>
}
