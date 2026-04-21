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
import com.forgex.mobile.feature.home.data.WorkbenchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val repository: WorkbenchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkbenchUiState())
    val uiState: StateFlow<WorkbenchUiState> = _uiState.asStateFlow()

    init {
        loadModules()
    }

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

    fun selectModule(moduleId: Long) {
        _uiState.update { it.copy(selectedModuleId = moduleId) }
        loadMenusForModule(moduleId)
    }

    fun clearModuleSelection() {
        _uiState.update { it.copy(selectedModuleId = null, menus = emptyList()) }
    }

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

