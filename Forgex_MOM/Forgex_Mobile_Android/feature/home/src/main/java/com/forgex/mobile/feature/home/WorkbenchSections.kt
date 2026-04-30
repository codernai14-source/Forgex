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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.core.ui.R

/**
 * 模块总览网格。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ModuleGrid(
    modules: List<CMenuVO>,
    onModuleClick: (CMenuVO) -> Unit
) {
    if (modules.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.home_empty_module),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 4
    ) {
        modules.take(12).forEach { module ->
            ModuleGridItem(
                module = module,
                onClick = { onModuleClick(module) }
            )
        }
    }
}

/**
 * 选中模块后的菜单列表区域。
 */
@Composable
internal fun ColumnScope.ModuleMenus(
    uiState: WorkbenchUiState,
    onBack: () -> Unit,
    onMenuClick: (CMenuVO) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back)
            )
        }
        val selectedModule = uiState.modules.find { it.id == uiState.selectedModuleId }
        Text(
            text = selectedModule?.name ?: stringResource(R.string.home_menu_default),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    when {
        uiState.isLoadingMenus -> WorkbenchLoadingPlaceholder()
        uiState.menus.isEmpty() -> WorkbenchEmptyPlaceholder(text = stringResource(R.string.home_empty_menu))
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.menus, key = { it.id ?: it.hashCode().toLong() }) { menu ->
                    MenuListItem(menu = menu, onClick = { onMenuClick(menu) })
                }
            }
        }
    }
}

/**
 * 工作台加载占位。
 */
@Composable
internal fun ColumnScope.WorkbenchLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 工作台空状态占位。
 */
@Composable
internal fun ColumnScope.WorkbenchEmptyPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 工作台模块卡片。
 */
@Composable
private fun ModuleGridItem(
    module: CMenuVO,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val moduleIcon = CMenuIconResolver.resolve(module)
                val gradientColors = moduleGradientColors(module)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = moduleIcon,
                            contentDescription = module.name,
                            tint = Color(0xFF1B4FA3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = module.name.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 工作台菜单列表项。
 */
@Composable
internal fun MenuListItem(
    menu: CMenuVO,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val menuIcon = CMenuIconResolver.resolve(menu)
            Icon(
                imageVector = menuIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menu.name.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (!menu.path.isNullOrBlank()) {
                    Text(
                        text = menu.path.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 为不同模块生成背景渐变色。
 */
private fun moduleGradientColors(module: CMenuVO): List<Color> {
    val path = module.path?.lowercase().orEmpty()
    return when {
        path.contains("workbench") -> listOf(Color(0xFFE8F0FF), Color(0xFFD7E5FF))
        path.contains("workflow") -> listOf(Color(0xFFEAF7F1), Color(0xFFD4EEE3))
        path.contains("message") -> listOf(Color(0xFFFFF2E2), Color(0xFFFFE2C2))
        path.contains("profile") -> listOf(Color(0xFFFCE8EE), Color(0xFFF7D4E0))
        path.contains("basic") -> listOf(Color(0xFFEEE7FF), Color(0xFFE0D4FF))
        else -> listOf(Color(0xFFEDEFF8), Color(0xFFDDE3F3))
    }
}
