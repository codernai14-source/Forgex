package com.forgex.mobile.core.model

import com.forgex.mobile.core.common.i18n.AppText

/**
 * 页面空态/错误态/加载态统一协议。
 */
data class FxPageFeedback(
    val isLoading: Boolean = false,
    val message: String? = null,
    val appText: AppText? = null
)
