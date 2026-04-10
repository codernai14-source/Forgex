package com.forgex.mobile.feature.workflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val WORKFLOW_ROUTE = "workflow"

@Composable
fun WorkflowScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "审批模块骨架", style = MaterialTheme.typography.headlineSmall)
        Text(text = "后续对接待我审批/我已审批/我发起")
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "返回")
        }
    }
}
