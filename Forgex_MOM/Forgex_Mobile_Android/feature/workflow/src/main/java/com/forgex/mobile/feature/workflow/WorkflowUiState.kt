package com.forgex.mobile.feature.workflow

import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO

data class WorkflowUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val executions: List<WfExecutionDTO> = emptyList()
)
