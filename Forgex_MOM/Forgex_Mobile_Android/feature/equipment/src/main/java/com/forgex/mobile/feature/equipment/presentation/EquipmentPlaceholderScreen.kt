package com.forgex.mobile.feature.equipment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.component.FxFeaturePlaceholderScreen

@Composable
fun EquipmentPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    FxFeaturePlaceholderScreen(
        title = "设备管理",
        summary = "设备管理模块已接入主导航，用于后续承接点检、保养、维修与设备台账能力。",
        plannedActions = listOf(
            "建立设备台账与点检任务页骨架",
            "复用统一列表页和表单页组件",
            "预留扫码识别设备编码的交互位置",
        ),
        modifier = modifier
    )
}
