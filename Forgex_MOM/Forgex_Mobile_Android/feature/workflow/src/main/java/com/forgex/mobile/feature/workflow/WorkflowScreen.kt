package com.forgex.mobile.feature.workflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.ui.R
import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entryMode.title(),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = entryMode.description(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.load(entryMode, force = true) }) {
                    Text(stringResource(R.string.common_refresh))
                }
                Button(onClick = onBack) {
                    Text(stringResource(R.string.common_back))
                }
            }
        }

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !uiState.errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: stringResource(R.string.workflow_load_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.load(entryMode, force = true) },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            uiState.executions.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.workflow_empty))
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.executions, key = { it.id ?: it.hashCode().toLong() }) { execution ->
                        WorkflowExecutionCard(execution = execution)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowExecutionCard(execution: WfExecutionDTO) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = execution.taskName.orEmpty().ifBlank { stringResource(R.string.workflow_no_name) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(stringResource(R.string.workflow_task_code, execution.taskCode.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
            Text(stringResource(R.string.workflow_initiator, execution.initiatorName.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
            Text(stringResource(R.string.workflow_node, execution.currentNodeName.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
            Text(stringResource(R.string.workflow_status, execution.status.toWorkflowStatusText()))
            Text(stringResource(R.string.workflow_start_time, execution.startTime.orEmpty().ifBlank { stringResource(R.string.placeholder_dash) }))
        }
    }
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
