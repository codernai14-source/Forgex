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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO

const val WORKFLOW_ROUTE = "workflow"
const val WORKFLOW_PENDING_ROUTE = "workflow/pending"
const val WORKFLOW_APPROVED_ROUTE = "workflow/approved"
const val WORKFLOW_MINE_ROUTE = "workflow/mine"

enum class WorkflowEntryMode(
    val title: String,
    val description: String
) {
    HOME("审批中心", "展示我待处理审批"),
    PENDING("待我审批", "当前需要我处理的审批单"),
    APPROVED("我已审批", "我已处理的审批单"),
    MINE("我发起的", "我发起的审批单")
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
                    text = entryMode.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = entryMode.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.load(entryMode, force = true) }) {
                    Text("刷新")
                }
                Button(onClick = onBack) {
                    Text("返回")
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
                        text = uiState.errorMessage ?: "审批列表加载失败",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.load(entryMode, force = true) },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("重试")
                    }
                }
            }

            uiState.executions.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("暂无审批数据")
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
                text = execution.taskName.orEmpty().ifBlank { "未命名审批任务" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("任务编码：${execution.taskCode.orEmpty().ifBlank { "--" }}")
            Text("发起人：${execution.initiatorName.orEmpty().ifBlank { "--" }}")
            Text("当前节点：${execution.currentNodeName.orEmpty().ifBlank { "--" }}")
            Text("状态：${execution.status.toWorkflowStatusText()}")
            Text("发起时间：${execution.startTime.orEmpty().ifBlank { "--" }}")
        }
    }
}

private fun Int?.toWorkflowStatusText(): String {
    return when (this) {
        0 -> "未审批"
        1 -> "审批中"
        2 -> "审批完成"
        3 -> "已驳回"
        else -> "未知"
    }
}
