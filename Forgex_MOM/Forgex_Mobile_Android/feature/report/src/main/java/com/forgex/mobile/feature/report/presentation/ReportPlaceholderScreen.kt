package com.forgex.mobile.feature.report.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun ReportPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "报表中心",
        summary = "报表中心模块已接入导航闭环，后续用于承接统计分析、图表看板与导出类能力。",
        plannedActions = listOf(
            "预留报表筛选、查询与统计卡片能力",
            "接入统一分页列表与表格展示协议",
            "为图表与导出能力保留页面结构",
        ),
        modifier = modifier
    )
}
