package com.forgex.mobile.feature.message

import com.forgex.mobile.core.common.i18n.AppText
import com.forgex.mobile.core.network.model.message.SysMessageVO

/**
 * 消息列表页 UI 状态。
 *
 * @param isLoading 是否加载中
 * @param errorMessage 错误信息
 * @param errorText 错误文本 (国际化)
 * @param messages 消息列表数据
 * @param readingIds 正在标记已读的消息 ID 集合
 * @param isFromCache 是否来自本地缓存 (离线模式)
 */
data class MessageUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorText: AppText? = null,
    val messages: List<SysMessageVO> = emptyList(),
    val readingIds: Set<Long> = emptySet(),
    val isFromCache: Boolean = false
)
