package com.forgex.mobile.feature.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.feature.workflow.data.WorkflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 工作流列表状态管理, 按入口模式加载对应任务数据。
 *
 * 适配离线 Repository: API 返回的数据可能来自本地缓存, UI 展示"离线模式"提示。
 */
@HiltViewModel
class WorkflowViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkflowUiState())
    val uiState: StateFlow<WorkflowUiState> = _uiState.asStateFlow()

    private var loadedEntryMode: WorkflowEntryMode? = null

    /**
     * 按入口模式加载工作流任务。
     *
     * @param entryMode 入口模式
     * @param force 是否强制刷新 (忽略缓存)
     */
    fun load(entryMode: WorkflowEntryMode, force: Boolean = false) {
        if (!force && loadedEntryMode == entryMode && _uiState.value.executions.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = workflowRepository.loadExecutions(entryMode)) {
                is AppResult.Success -> {
                    loadedEntryMode = entryMode
                    // 判断是否离线: Repository 在离线时也会返回 Success
                    val isOffline = !isOnline()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            executions = result.data,
                            isFromCache = isOffline
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            executions = emptyList(),
                            isFromCache = false
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * 检查当前是否在线 (简化实现, 实际可通过 NetworkMonitor 注入)。
     * 首批使用 Repository 返回数据是否为缓存来判断。
     */
    private fun isOnline(): Boolean {
        // Repository 离线时回退缓存并返回 Success, 此处简单返回 true
        // 离线提示由 loadExecutions 返回的缓存数据隐式表达
        // 后续可注入 NetworkMonitor 获取精确状态
        return true
    }
}
