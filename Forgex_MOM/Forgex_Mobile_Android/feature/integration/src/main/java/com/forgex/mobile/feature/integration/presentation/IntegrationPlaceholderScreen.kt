package com.forgex.mobile.feature.integration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun IntegrationPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "集成平台",
        summary = "集成平台模块已纳入升级工程，用于承接第三方系统对接、接口配置与联调入口。",
        plannedActions = listOf(
            "沉淀接口配置与联调入口页面",
            "复用统一表单与状态反馈组件",
            "为上传下载与日志查询能力预留扩展点",
        ),
        modifier = modifier
    )
}
