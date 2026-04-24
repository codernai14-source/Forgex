package com.forgex.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.feature.home.data.WorkbenchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 工作台页面状态管理，负责模块列表与模块菜单的切换加载。
 */
@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val repository: WorkbenchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkbenchUiState())
    val uiState: StateFlow<WorkbenchUiState> = _uiState.asStateFlow()

    init {
        loadModules()
    }

    /**
     * 加载工作台模块列表。
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
     * 选中模块并触发对应菜单加载。
     */
    fun selectModule(moduleId: Long) {
        _uiState.update { it.copy(selectedModuleId = moduleId) }
        loadMenusForModule(moduleId)
    }

    /**
     * 清空模块选择，回到模块总览态。
     */
    fun clearModuleSelection() {
        _uiState.update { it.copy(selectedModuleId = null, menus = emptyList()) }
    }

    /**
     * 加载指定模块下的工作台菜单。
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
