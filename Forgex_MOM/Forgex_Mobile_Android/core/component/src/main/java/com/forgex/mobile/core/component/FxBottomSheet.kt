package com.forgex.mobile.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.forgex.mobile.core.designsystem.FxSpacing
import com.forgex.mobile.core.designsystem.rememberFxAdaptiveMetrics
import com.forgex.mobile.core.ui.R

/**
 * 标准底部弹层组件。
 *
 * 统一承接操作面板、筛选面板和轻量表单弹层，
 * 并根据手机与 Pad 自动收口内容最大宽度。
 *
 * @param visible 是否显示弹层
 * @param onDismiss 关闭弹层回调
 * @param modifier 组件修饰符
 * @param title 标题文案
 * @param subtitle 说明文案
 * @param confirmText 确认按钮文案
 * @param onConfirm 确认回调
 * @param dismissText 取消按钮文案
 * @param onDismissAction 底部取消按钮回调
 * @param confirmEnabled 确认按钮是否可用
 * @param dismissEnabled 取消按钮是否可用
 * @param loading 是否处于提交中
 * @param showDragHandle 是否显示拖拽手柄
 * @param footer 自定义底部区域
 * @param content 主内容区域
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismissAction: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    loading: Boolean = false,
    showDragHandle: Boolean = true,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) {
        return
    }

    val metrics = rememberFxAdaptiveMetrics()
    val resolvedDismissText = dismissText ?: stringResource(R.string.common_cancel)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = if (showDragHandle) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = metrics.formMaxWidth)
                    .padding(
                        start = FxSpacing.large,
                        end = FxSpacing.large,
                        bottom = FxSpacing.xLarge
                    ),
                verticalArrangement = Arrangement.spacedBy(FxSpacing.large)
            ) {
                if (!title.isNullOrBlank() || !subtitle.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(FxSpacing.xSmall)) {
                        if (!title.isNullOrBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                content()

                when {
                    footer != null -> footer()
                    confirmText != null && onConfirm != null -> {
                        FxFormFooterBar(
                            confirmText = confirmText,
                            onConfirm = onConfirm,
                            secondaryText = resolvedDismissText,
                            onSecondary = onDismissAction ?: onDismiss,
                            confirmEnabled = confirmEnabled,
                            secondaryEnabled = dismissEnabled,
                            loading = loading
                        )
                    }
                }
            }
        }
    }
}

/**
 * 底部弹层操作项模型。
 *
 * 用于描述操作面板中的单个动作，统一标题、副标题和危险样式标记。
 *
 * @param title 主标题
 * @param subtitle 副标题
 * @param enabled 是否可点击
 * @param destructive 是否为危险操作
 */
@Immutable
data class FxBottomSheetAction(
    val title: String,
    val subtitle: String? = null,
    val enabled: Boolean = true,
    val destructive: Boolean = false
)

/**
 * 标准底部弹层操作列表。
 *
 * 适用于“更多操作”“筛选入口”“快捷处理”等菜单型弹层，
 * 业务页面只需要提供动作集合和点击回调。
 *
 * @param actions 操作项列表
 * @param onActionClick 操作点击回调
 * @param modifier 组件修饰符
 */
@Composable
fun FxBottomSheetActionList(
    actions: List<FxBottomSheetAction>,
    onActionClick: (FxBottomSheetAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        actions.forEachIndexed { index, action ->
            if (index > 0) {
                HorizontalDivider()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = action.enabled) { onActionClick(action) }
                    .padding(vertical = FxSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(FxSpacing.xSmall)
            ) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (action.destructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (!action.subtitle.isNullOrBlank()) {
                    Text(
                        text = action.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
