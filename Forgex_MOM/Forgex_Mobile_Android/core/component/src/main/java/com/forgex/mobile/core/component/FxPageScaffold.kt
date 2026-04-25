package com.forgex.mobile.core.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.forgex.mobile.core.designsystem.rememberFxAdaptiveMetrics

/**
 * 标准页面骨架组件。
 *
 * 统一处理顶部标题栏、内容区内边距和底部操作区，
 * 让业务页面只关注自身内容编排。
 *
 * @param title 页面标题
 * @param modifier 页面容器修饰符
 * @param topBarActions 顶栏右侧操作区
 * @param bottomBar 底部操作区
 * @param content 页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxPageScaffold(
    title: String? = null,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val metrics = rememberFxAdaptiveMetrics()

    Scaffold(
        topBar = {
            if (!title.isNullOrBlank()) {
                TopAppBar(
                    title = { Text(title) },
                    actions = topBarActions
                )
            }
        },
        bottomBar = { bottomBar?.invoke() }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = metrics.contentPadding)
            ) {
                content(innerPadding)
            }
        }
    }
}
