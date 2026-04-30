package com.forgex.mobile.feature.production.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun ProductionPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "生产管理",
        summary = "生产管理模块已接入当前安卓升级框架，后续用于承接工单、报工、派工与生产执行场景。",
        plannedActions = listOf(
            "建立生产任务列表与详情骨架",
            "接入状态角标、表格与分页组件",
            "为工序扫码与批量操作预留入口",
        ),
        modifier = modifier
    )
}
