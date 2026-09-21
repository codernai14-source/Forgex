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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.workbench.CMenuBundleRepository
import com.forgex.mobile.feature.home.data.WorkbenchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 工作台页面状态管理：C 端菜单聚合包缓存优先渲染，模块菜单本地切换。
 */
@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val repository: WorkbenchRepository,
    private val cMenuBundleRepository: CMenuBundleRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkbenchUiState())
    val uiState: StateFlow<WorkbenchUiState> = _uiState.asStateFlow()

    init {
        loadBundle()
    }

    /**
     * 加载 C 端菜单聚合包（授权模块整树 + 收藏）：
     * 命中缓存（选租户后预载）零网络直接渲染，未命中走网络拉取，失败回退旧两级接口。
     */
    fun loadBundle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModules = true, errorMessage = null) }

            val tenantId = sessionStore.tenantId.first()
            if (tenantId.isNullOrBlank()) {
                // 无租户上下文（异常场景），走旧接口兜底
                loadModules()
                return@launch
            }

            // 1. 缓存优先：选租户后已预载
            val cached = cMenuBundleRepository.readCachedBundle(tenantId)
            if (cached != null) {
                _uiState.update {
                    it.copy(isLoadingModules = false, modules = cached.modules, errorMessage = null)
                }
                return@launch
            }

            // 2. 网络拉取聚合包（成功后自动写入缓存）
            when (val result = cMenuBundleRepository.fetchBundle(tenantId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(isLoadingModules = false, modules = result.data.modules)
                    }
                }
                is AppResult.Error -> {
                    // 聚合包不可用时回退旧的两级接口，保证可用性
                    loadModules()
                }
                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * 强制刷新：跳过缓存重新拉取聚合包（下拉刷新/重试共用）。
     */
    fun refresh() {
        viewModelScope.launch {
            val tenantId = sessionStore.tenantId.first()
            if (tenantId.isNullOrBlank()) {
                loadModules()
                return@launch
            }
            _uiState.update { it.copy(isLoadingModules = true) }
            when (val result = cMenuBundleRepository.fetchBundle(tenantId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(isLoadingModules = false, modules = result.data.modules, errorMessage = null)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoadingModules = false, errorMessage = result.message)
                    }
                }
                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * 加载工作台模块列表（旧接口兜底）。
     */
    fun loadModules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModules = true, errorMessage = null) }
            when (val result = repository.loadModules()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingModules = false,
                            modules = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoadingModules = false, errorMessage = result.message)
                    }
                }
                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoadingModules = true) }
                }
            }
        }
    }

    /**
     * 选中模块：聚合包内模块自带 children 时本地切换（零网络），否则回退旧接口加载。
     */
    fun selectModule(moduleId: Long) {
        _uiState.update { it.copy(selectedModuleId = moduleId) }

        val children = _uiState.value.modules
            .firstOrNull { it.id == moduleId }
            ?.children
        if (!children.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoadingMenus = false, menus = children) }
            return
        }
        loadMenusForModule(moduleId)
    }

    /**
     * 清空模块选择，回到模块总览态。
     */
    fun clearModuleSelection() {
        _uiState.update { it.copy(selectedModuleId = null, menus = emptyList()) }
    }

    /**
     * 加载指定模块下的工作台菜单（旧接口兜底）。
     */
    private fun loadMenusForModule(moduleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMenus = true) }
            when (val result = repository.loadMenus(moduleId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(isLoadingMenus = false, menus = result.data)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoadingMenus = false, errorMessage = result.message)
                    }
                }
                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoadingMenus = true) }
                }
            }
        }
    }
}
