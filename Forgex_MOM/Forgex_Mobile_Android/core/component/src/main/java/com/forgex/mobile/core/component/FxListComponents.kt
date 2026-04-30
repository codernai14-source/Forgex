package com.forgex.mobile.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.architecture.PagingUiState

/**
 * 列表页骨架组件。
 *
 * 统一处理加载态、错误态和空态，让业务页只关心列表头部与数据项内容。
 *
 * @param title 页面标题
 * @param loading 是否处于加载中
 * @param error 错误文案
 * @param emptyText 空态文案
 * @param hasData 是否有数据
 * @param onRetry 重试回调
 * @param content 实际列表内容
 */
@Composable
fun FxListPage(
    title: String,
    loading: Boolean,
    error: String?,
    emptyText: String,
    hasData: Boolean,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    FxPageScaffold(title = title) {
        when {
            loading -> FxLoadingView()
            !error.isNullOrBlank() -> FxErrorView(message = error, onRetry = onRetry)
            !hasData -> FxEmptyView(message = emptyText)
            else -> content()
        }
    }
}

/**
 * 标准分页列表组件。
 *
 * 统一消费分页状态协议，自动处理空态、错误态和数据渲染。
 *
 * @param state 分页状态
 * @param modifier 组件修饰符
 * @param emptyText 空态文案
 * @param onRetry 重试回调
 * @param contentPadding 列表内边距
 * @param itemKey 列表项键
 * @param itemContent 列表项内容
 */
@Composable
fun <T> FxPagedList(
    state: PagingUiState<T>,
    modifier: Modifier = Modifier,
    emptyText: String,
    onRetry: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 0.dp),
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val errorMessage = state.error

    when {
        state.isRefreshing && state.list.isEmpty() -> FxLoadingView(modifier = modifier)
        !errorMessage.isNullOrBlank() && state.list.isEmpty() -> FxErrorView(
            message = errorMessage,
            onRetry = onRetry,
            modifier = modifier
        )
        state.list.isEmpty() -> FxEmptyView(message = emptyText, modifier = modifier)
        itemKey != null -> {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = contentPadding
            ) {
                items(items = state.list, key = itemKey) { item ->
                    itemContent(item)
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = contentPadding
            ) {
                itemsIndexed(state.list) { _, item ->
                    itemContent(item)
                }
            }
        }
    }
}

/**
 * 向后兼容的简化分页列表容器。
 *
 * @param content 自定义内容
 */
@Composable
fun FxPagedList(content: @Composable () -> Unit) {
    content()
}

/**
 * 标准列表卡片项。
 *
 * 统一处理标题、副标题、状态和尾部操作区，适用于大多数业务列表场景。
 *
 * @param title 主标题
 * @param subtitle 副标题
 * @param status 状态文案
 * @param modifier 组件修饰符
 * @param onClick 点击回调
 * @param trailing 尾部内容
 */
@Composable
fun FxListItem(
    title: String,
    subtitle: String? = null,
    status: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!status.isNullOrBlank()) {
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        FxStatusBadge(text = status)
                    }
                }
            }
            trailing?.invoke()
        }
    }
}
