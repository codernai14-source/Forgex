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

@HiltViewModel
class WorkflowViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkflowUiState())
    val uiState: StateFlow<WorkflowUiState> = _uiState.asStateFlow()

    private var loadedEntryMode: WorkflowEntryMode? = null

    fun load(entryMode: WorkflowEntryMode, force: Boolean = false) {
        if (!force && loadedEntryMode == entryMode && _uiState.value.executions.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = workflowRepository.loadExecutions(entryMode)) {
                is AppResult.Success -> {
                    loadedEntryMode = entryMode
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            executions = result.data
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            executions = emptyList()
                        )
                    }
                }

                AppResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
