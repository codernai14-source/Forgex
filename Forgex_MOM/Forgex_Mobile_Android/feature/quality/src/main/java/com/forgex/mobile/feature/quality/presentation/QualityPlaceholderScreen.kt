package com.forgex.mobile.feature.quality.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun QualityPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "质量管理",
        summary = "质量管理模块已具备可访问的占位页，后续将在此承接检验、质检记录与异常处理场景。",
        plannedActions = listOf(
            "搭建检验单列表与质检表单结构",
            "接入日期范围、状态标签与结果反馈组件",
            "为不合格处理流程保留页面扩展位",
        ),
        modifier = modifier
    )
}
