package com.forgex.mobile.core.common.i18n

import androidx.annotation.StringRes

sealed interface AppText {
    data class Raw(val value: String) : AppText

    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : AppText

    data class Dynamic(
        val key: String,
        @StringRes val fallbackResId: Int? = null,
        val fallbackRaw: String? = null,
        val args: List<Any> = emptyList()
    ) : AppText
}
