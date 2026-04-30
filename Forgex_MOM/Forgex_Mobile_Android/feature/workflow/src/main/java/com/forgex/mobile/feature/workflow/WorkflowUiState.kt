package com.forgex.mobile.feature.workflow

import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO

/**
 * 工作流列表页 UI 状态。
 */
data class WorkflowUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val executions: List<WfExecutionDTO> = emptyList()
)
