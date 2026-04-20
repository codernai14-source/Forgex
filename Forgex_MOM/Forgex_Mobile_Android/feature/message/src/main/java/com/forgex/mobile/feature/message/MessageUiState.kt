package com.forgex.mobile.feature.message

import com.forgex.mobile.core.common.i18n.AppText
import com.forgex.mobile.core.network.model.message.SysMessageVO

data class MessageUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorText: AppText? = null,
    val messages: List<SysMessageVO> = emptyList(),
    val readingIds: Set<Long> = emptySet()
)
