package com.forgex.mobile.feature.basic.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun BasicPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "基础资料",
        summary = "基础资料模块已接入企业级升级骨架，后续将在这里承接组织、字典、档案与通用主数据能力。",
        plannedActions = listOf(
            "建立 data/domain/presentation 分层并承接基础资料接口",
            "补齐主数据列表、详情与表单页面骨架",
            "接入统一字典、分页与表格组件",
        ),
        modifier = modifier
    )
}
