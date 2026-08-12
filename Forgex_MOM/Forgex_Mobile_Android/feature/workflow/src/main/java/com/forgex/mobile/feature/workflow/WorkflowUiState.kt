package com.forgex.mobile.feature.workflow

import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO

/**
 * 工作流列表页 UI 状态。
 *
 * @param isLoading 是否加载中
 * @param errorMessage 错误信息
 * @param executions 审批列表数据
 * @param isFromCache 是否来自本地缓存 (离线模式)
 */
data class WorkflowUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val executions: List<WfExecutionDTO> = emptyList(),
    val isFromCache: Boolean = false
)
