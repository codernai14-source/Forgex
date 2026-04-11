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

package com.forgex.mobile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.feature.home.FavoritesScreen
import com.forgex.mobile.feature.home.WorkbenchScreen
import com.forgex.mobile.feature.message.MessageEntryMode
import com.forgex.mobile.feature.message.MessageScreen
import com.forgex.mobile.feature.profile.ProfileScreen

/**
 * 底部 5-Tab 的数据定义。
 * 按需求顺序：收藏、通知、工作台、消息、我的。
 * 默认选中"工作台"（index=2）。
 */
enum class MainTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    FAVORITES("收藏", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    NOTIFICATION("通知", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    WORKBENCH("工作台", Icons.Filled.Widgets, Icons.Outlined.Widgets),
    MESSAGE("消息", Icons.Filled.Mail, Icons.Outlined.Mail),
    PROFILE("我的", Icons.Filled.Person, Icons.Outlined.Person);
}

private const val DEFAULT_TAB_INDEX = 2 // 工作台

/**
 * 首页主壳层：底部 5 Tab 导航。
 *
 * @param onMenuClick 工作台/收藏中菜单点击回调，由外层 NavHost 负责路由分发。
 * @param onLogout    退出登录回调。
 */
@Composable
fun MainShellScreen(
    onMenuClick: (CMenuVO) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(DEFAULT_TAB_INDEX) }
    val tabs = MainTab.entries

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedTabIndex
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = if (tab == MainTab.WORKBENCH) Modifier.size(28.dp) else Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        val tabModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (tabs[selectedTabIndex]) {
            MainTab.FAVORITES -> {
                FavoritesScreen(
                    onMenuClick = onMenuClick,
                    modifier = tabModifier
                )
            }

            MainTab.NOTIFICATION -> {
                // 通知 tab 复用消息页面的 UNREAD 模式
                MessageScreen(
                    entryMode = MessageEntryMode.UNREAD,
                    onBack = { /* 底部 tab 不需要返回 */ },
                    modifier = tabModifier
                )
            }

            MainTab.WORKBENCH -> {
                WorkbenchScreen(
                    onMenuClick = onMenuClick,
                    modifier = tabModifier
                )
            }

            MainTab.MESSAGE -> {
                MessageScreen(
                    entryMode = MessageEntryMode.HOME,
                    onBack = { /* 底部 tab 不需要返回 */ },
                    modifier = tabModifier
                )
            }

            MainTab.PROFILE -> {
                ProfileScreen(
                    onBack = { /* 底部 tab 不需要返回 */ },
                    modifier = tabModifier
                )
            }
        }
    }
}

