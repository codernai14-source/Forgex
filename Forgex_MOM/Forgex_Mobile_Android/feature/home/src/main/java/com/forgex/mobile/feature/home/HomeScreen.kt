package com.forgex.mobile.feature.home

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
import com.forgex.mobile.core.ui.device.DeviceType

const val HOME_ROUTE = "home"

@Composable
fun HomeScreen(
    deviceType: DeviceType,
    onOpenWorkflow: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "首页骨架 (${deviceType.name})", style = MaterialTheme.typography.headlineSmall)
        Text(text = "菜单与路由后续由后端 componentKey 驱动")
        Button(onClick = onOpenWorkflow) { Text("进入审批") }
        Button(onClick = onOpenMessage) { Text("进入消息") }
        Button(onClick = onOpenProfile) { Text("进入个人中心") }
    }
}
