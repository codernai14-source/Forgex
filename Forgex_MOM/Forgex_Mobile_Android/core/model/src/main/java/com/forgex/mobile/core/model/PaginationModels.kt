package com.forgex.mobile.core.model

/**
 * 通用分页请求参数。
 */
data class FxPageRequest(
    val pageNum: Int = 1,
    val pageSize: Int = 20
)

/**
 * 通用分页响应模型，屏蔽不同业务接口差异。
 */
data class FxPageData<T>(
    val records: List<T> = emptyList(),
    val total: Long = 0L,
    val size: Long = 0L,
    val current: Long = 1L,
    val pages: Long = 0L
)

/**
 * 表格列基础协议。
 */
data class FxTableColumn(
    val key: String,
    val title: String,
    val width: Int? = null,
    val align: FxTableAlign = FxTableAlign.START,
    val sortable: Boolean = false,
    val ellipsis: Boolean = false,
    val type: String? = null
)

enum class FxTableAlign {
    START,
    CENTER,
    END
}
