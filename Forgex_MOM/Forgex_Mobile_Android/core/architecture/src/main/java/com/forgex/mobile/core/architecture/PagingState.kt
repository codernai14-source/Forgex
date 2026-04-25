package com.forgex.mobile.core.architecture

import com.forgex.mobile.core.common.i18n.AppText
import com.forgex.mobile.core.model.FxPageData

/**
 * 统一分页状态协议，避免各业务模块重复定义分页字段。
 */
data class PagingUiState<T>(
    val list: List<T> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val total: Long = 0L,
    val error: String? = null,
    val errorText: AppText? = null
) : UiState {
    companion object {
        fun <T> fromPage(pageData: FxPageData<T>): PagingUiState<T> {
            val hasMore = pageData.current < pageData.pages
            return PagingUiState(
                list = pageData.records,
                total = pageData.total,
                hasMore = hasMore
            )
        }
    }
}
