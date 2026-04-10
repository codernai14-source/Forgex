package com.forgex.mobile.core.common.model

data class ApiResponse<T>(
    val code: Int? = null,
    val message: String? = null,
    val msg: String? = null,
    val data: T? = null
) {
    fun isSuccess(): Boolean = code == 200

    fun errorMessage(): String = message ?: msg ?: "Unknown error"
}
