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
 * 字典选项协议。value 用于提交，label 用于展示，tagColor 可选用于状态标签。
 */
data class FxDictionaryOption(
    val value: String,
    val label: String,
    val tagColor: String? = null,
    val disabled: Boolean = false
)

/**
 * 字典值解析器，统一处理空值和后端未返回标签的降级展示。
 */
class FxDictionaryResolver(private val options: List<FxDictionaryOption>) {
    private val byValue = options.associateBy { it.value }

    fun find(value: String?): FxDictionaryOption? = value?.let(byValue::get)

    fun label(value: String?, fallback: String = "-"): String {
        return find(value)?.label ?: value?.takeIf { it.isNotBlank() } ?: fallback
    }
}

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
