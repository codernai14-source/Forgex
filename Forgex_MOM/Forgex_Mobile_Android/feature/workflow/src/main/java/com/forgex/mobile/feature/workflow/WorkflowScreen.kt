package com.forgex.mobile.feature.workflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.component.FxListItem
import com.forgex.mobile.core.component.FxListPage
import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO
import com.forgex.mobile.core.ui.R

const val WORKFLOW_ROUTE = "workflow"
const val WORKFLOW_PENDING_ROUTE = "workflow/pending"
const val WORKFLOW_APPROVED_ROUTE = "workflow/approved"
const val WORKFLOW_MINE_ROUTE = "workflow/mine"

enum class WorkflowEntryMode {
    HOME,
    PENDING,
    APPROVED,
    MINE
}

/**
 * 工作流列表页，统一承接首页、待办、已办和我发起的入口。
 */
@Composable
fun WorkflowScreen(
    entryMode: WorkflowEntryMode = WorkflowEntryMode.HOME,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkflowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(entryMode) {
        viewModel.load(entryMode)
    }

    FxListPage(
        title = entryMode.title(),
        loading = uiState.isLoading,
        error = uiState.errorMessage,
        emptyText = stringResource(R.string.workflow_empty),
        hasData = uiState.executions.isNotEmpty(),
        onRetry = { viewModel.load(entryMode, force = true) }
    ) {
        Column(
            modifier = modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entryMode.description(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.load(entryMode, force = true) }) {
                        Text(stringResource(R.string.common_refresh))
                    }
                    Button(onClick = onBack) {
                        Text(stringResource(R.string.common_back))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.executions, key = { it.id ?: it.hashCode().toLong() }) { execution ->
                    WorkflowExecutionCard(execution = execution)
                }
            }
        }
    }
}

/**
 * 工作流任务卡片，复用统一列表项展示任务关键信息。
 */
@Composable
private fun WorkflowExecutionCard(execution: WfExecutionDTO) {
    val subtitle = buildString {
        append(stringResource(R.string.workflow_task_code, execution.taskCode.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        append("\n")
        append(stringResource(R.string.workflow_initiator, execution.initiatorName.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        append("\n")
        append(stringResource(R.string.workflow_node, execution.currentNodeName.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        append("\n")
        append(stringResource(R.string.workflow_start_time, execution.startTime.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
    }

    FxListItem(
        title = execution.taskName.orEmpty().ifBlank { stringResource(R.string.workflow_no_name) },
        subtitle = subtitle,
        status = execution.status.toWorkflowStatusText()
    )
}

@Composable
private fun WorkflowEntryMode.title(): String {
    return when (this) {
        WorkflowEntryMode.HOME -> stringResource(R.string.workflow_center_title)
        WorkflowEntryMode.PENDING -> stringResource(R.string.workflow_pending_title)
        WorkflowEntryMode.APPROVED -> stringResource(R.string.workflow_approved_title)
        WorkflowEntryMode.MINE -> stringResource(R.string.workflow_mine_title)
    }
}

@Composable
private fun WorkflowEntryMode.description(): String {
    return when (this) {
        WorkflowEntryMode.HOME -> stringResource(R.string.workflow_center_desc)
        WorkflowEntryMode.PENDING -> stringResource(R.string.workflow_pending_desc)
        WorkflowEntryMode.APPROVED -> stringResource(R.string.workflow_approved_desc)
        WorkflowEntryMode.MINE -> stringResource(R.string.workflow_mine_desc)
    }
}

@Composable
private fun Int?.toWorkflowStatusText(): String {
    return when (this) {
        0 -> stringResource(R.string.workflow_status_pending)
        1 -> stringResource(R.string.workflow_status_running)
        2 -> stringResource(R.string.workflow_status_done)
        3 -> stringResource(R.string.workflow_status_rejected)
        else -> stringResource(R.string.workflow_status_unknown)
    }
}
