/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.mobile.feature.home

import com.forgex.mobile.core.network.model.workbench.CMenuVO

/**
 * 工作台 UI 状态
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
 * 收藏 UI 状态
 */
data class FavoritesUiState(
    val isLoading: Boolean = false,
    val favorites: List<CMenuVO> = emptyList(),
    val togglingIds: Set<Long> = emptySet(),
    val errorMessage: String? = null
)

