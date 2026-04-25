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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.core.ui.R

/**
 * 工作台页面，优先展示后端模块与菜单；未配置完整菜单时展示默认升级入口。
 */
@Composable
fun WorkbenchScreen(
    onMenuClick: (CMenuVO) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkbenchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickEntries = WorkbenchQuickEntries.items()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        WorkbenchErrorBanner(
            errorMessage = uiState.errorMessage,
            onRetry = viewModel::loadModules
        )

        when {
            uiState.isLoadingModules -> WorkbenchLoadingPlaceholder()
            uiState.selectedModuleId == null -> {
                ModuleGrid(
                    modules = uiState.modules,
                    onModuleClick = { module ->
                        module.id?.let(viewModel::selectModule)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (quickEntries.isNotEmpty()) {
                    Text(
                        text = "企业升级入口",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickEntries, key = { it.id ?: it.hashCode().toLong() }) { menu ->
                            MenuListItem(menu = menu, onClick = { onMenuClick(menu) })
                        }
                    }
                } else {
                    WorkbenchEmptyPlaceholder(text = stringResource(R.string.home_select_module))
                }
            }

            else -> {
                ModuleMenus(
                    uiState = uiState,
                    onBack = viewModel::clearModuleSelection,
                    onMenuClick = onMenuClick
                )
            }
        }
    }
}

@Composable
private fun WorkbenchErrorBanner(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    if (errorMessage.isNullOrBlank()) {
        return
    }

    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error
    )
    Button(
        onClick = onRetry,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(stringResource(R.string.common_retry))
    }
    Spacer(modifier = Modifier.height(12.dp))
}
