package com.forgex.mobile.feature.label.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun LabelPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "标签管理",
        summary = "标签管理模块已经具备可进入页面，后续将在这里承接条码、标签模板与打印作业能力。",
        plannedActions = listOf(
            "沉淀标签任务列表与模板配置页面",
            "对接扫码输入与打印前校验流程",
            "为文件上传、模板预览与结果反馈预留入口",
        ),
        modifier = modifier
    )
}
