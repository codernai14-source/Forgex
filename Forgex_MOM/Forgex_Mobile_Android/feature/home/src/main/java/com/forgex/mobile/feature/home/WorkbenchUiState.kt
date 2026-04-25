package com.forgex.mobile.feature.home

import com.forgex.mobile.core.network.model.workbench.CMenuVO

/**
 * 工作台页面状态。
 */
data class WorkbenchUiState(
    val isLoadingModules: Boolean = false,
    val isLoadingMenus: Boolean = false,
    val modules: List<CMenuVO> = emptyList(),
    val selectedModuleId: Long? = null,
    val menus: List<CMenuVO> = emptyList(),
    val errorMessage: String? = null
)

/**
 * 收藏页状态。
 */
data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favorites: List<CMenuVO> = emptyList(),
    val togglingIds: Set<Long> = emptySet(),
    val errorMessage: String? = null
)
