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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgex.mobile.R
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.feature.home.FavoritesScreen
import com.forgex.mobile.feature.home.WorkbenchScreen
import com.forgex.mobile.feature.message.MessageEntryMode
import com.forgex.mobile.feature.message.MessageScreen
import com.forgex.mobile.feature.profile.ProfileScreen

enum class MainTab(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    FAVORITES(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    NOTIFICATION(Icons.Filled.Notifications, Icons.Outlined.Notifications),
    WORKBENCH(Icons.Filled.Home, Icons.Outlined.Home),
    MESSAGE(Icons.Filled.Email, Icons.Outlined.Email),
    PROFILE(Icons.Filled.Person, Icons.Outlined.Person);
}

private const val DEFAULT_TAB_INDEX = 2

@Composable
fun MainShellScreen(
    onMenuClick: (CMenuVO) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(DEFAULT_TAB_INDEX) }
    val tabs = MainTab.entries
    val labels = listOf(
        stringResource(R.string.tab_favorites),
        stringResource(R.string.tab_notification),
        stringResource(R.string.tab_workbench),
        stringResource(R.string.tab_message),
        stringResource(R.string.tab_profile)
    )

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
                                contentDescription = labels[index],
                                modifier = if (tab == MainTab.WORKBENCH) Modifier.size(28.dp) else Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = labels[index],
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
                MessageScreen(
                    entryMode = MessageEntryMode.UNREAD,
                    onBack = { },
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
                    onBack = { },
                    modifier = tabModifier
                )
            }

            MainTab.PROFILE -> {
                ProfileScreen(
                    onBack = onLogout,
                    modifier = tabModifier
                )
            }
        }
    }
}
