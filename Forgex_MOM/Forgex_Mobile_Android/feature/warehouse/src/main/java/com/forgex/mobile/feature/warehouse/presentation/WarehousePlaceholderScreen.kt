package com.forgex.mobile.feature.warehouse.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun WarehousePlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "仓储管理",
        summary = "仓储管理模块已具备独立导航入口，后续将在此承接入库、出库、盘点与库位作业流程。",
        plannedActions = listOf(
            "接入扫码协议与仓储作业表单骨架",
            "补齐分页任务列表与明细页",
            "为 PDA 作业场景预留高频交互入口",
        ),
        modifier = modifier
    )
}
