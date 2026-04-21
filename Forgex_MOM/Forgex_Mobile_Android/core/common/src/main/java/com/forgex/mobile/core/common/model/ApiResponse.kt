package com.forgex.mobile.core.common.model

data class ApiResponse<T>(
    val code: Int? = null,
    val message: String? = null,
    val msg: String? = null,
    val data: T? = null,
    val messageCode: String? = null,
    val i18n: ApiResponseI18nMeta? = null
) {
    fun isSuccess(): Boolean = code == 200

    fun errorMessage(): String = message ?: msg ?: "Unknown error"
}

data class ApiResponseI18nMeta(
    val module: String? = null,
    val code: String? = null,
    val args: List<String>? = null
)
