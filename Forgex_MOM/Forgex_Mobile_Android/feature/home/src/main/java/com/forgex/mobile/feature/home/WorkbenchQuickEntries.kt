package com.forgex.mobile.feature.home

import com.forgex.mobile.core.navigation.FeatureRoutes
import com.forgex.mobile.core.network.model.workbench.CMenuVO

/**
 * 工作台默认快捷入口，在后端菜单未完全配置前提供企业级升级模块入口。
 */
internal object WorkbenchQuickEntries {

    /**
     * 生成默认快捷入口列表。
     */
    fun items(): List<CMenuVO> {
        return listOf(
            quickEntry(10001L, "基础资料", FeatureRoutes.BASIC, "BasicScreen"),
            quickEntry(10002L, "报表中心", FeatureRoutes.REPORT, "ReportScreen"),
            quickEntry(10003L, "集成平台", FeatureRoutes.INTEGRATION, "IntegrationScreen"),
            quickEntry(10004L, "仓储管理", FeatureRoutes.WAREHOUSE, "WarehouseScreen"),
            quickEntry(10005L, "生产管理", FeatureRoutes.PRODUCTION, "ProductionScreen"),
            quickEntry(10006L, "质量管理", FeatureRoutes.QUALITY, "QualityScreen"),
            quickEntry(10007L, "设备管理", FeatureRoutes.EQUIPMENT, "EquipmentScreen"),
            quickEntry(10008L, "标签管理", FeatureRoutes.LABEL, "LabelScreen")
        )
    }

    private fun quickEntry(
        id: Long,
        name: String,
        path: String,
        componentKey: String
    ): CMenuVO {
        return CMenuVO(
            id = id,
            moduleId = 0L,
            name = name,
            path = path,
            componentKey = componentKey,
            menuMode = "native",
            visible = true,
            status = true
        )
    }
}
