package com.forgex.mobile.core.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.forgex.mobile.core.designsystem.FxSpacing
import com.forgex.mobile.core.ui.R

/**
 * 标准确认对话框。
 *
 * 统一处理二次确认场景中的标题、正文、补充说明和危险操作样式，
 * 让删除、提交、切换状态等交互保持一致。
 *
 * @param title 标题文案
 * @param message 主要说明文案
 * @param onConfirm 确认回调
 * @param onDismiss 取消或关闭回调
 * @param modifier 组件修饰符
 * @param confirmText 确认按钮文案
 * @param dismissText 取消按钮文案
 * @param supportingText 补充说明文案
 * @param confirmEnabled 确认按钮是否可用
 * @param dismissEnabled 取消按钮是否可用
 * @param destructive 是否为危险操作
 * @param icon 头部图标
 */
@Composable
fun FxConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String? = null,
    dismissText: String? = null,
    supportingText: String? = null,
    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    destructive: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    val resolvedConfirmText = confirmText ?: stringResource(R.string.common_confirm)
    val resolvedDismissText = dismissText ?: stringResource(R.string.common_cancel)
    val bodyContent: (@Composable () -> Unit)? = if (message.isBlank() && supportingText.isNullOrBlank()) {
        null
    } else {
        {
            Column(verticalArrangement = Arrangement.spacedBy(FxSpacing.small)) {
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    val confirmColors = if (destructive) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    } else {
        ButtonDefaults.buttonColors()
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = icon,
        title = { Text(title) },
        text = bodyContent,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = confirmColors
            ) {
                Text(resolvedConfirmText)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = dismissEnabled
            ) {
                Text(resolvedDismissText)
            }
        }
    )
}

/**
 * 简化版系统 Toast 触发器。
 *
 * 通过组合态安全触发原生 Toast，适用于一次性轻提示场景，
 * 避免业务页面自行持有 `Context`。
 *
 * @param message 提示文案
 * @param show 是否立即展示
 * @param duration Toast 展示时长
 * @param onShown 已触发展示后的回调
 */
@Composable
fun FxToast(
    message: String,
    show: Boolean = true,
    duration: Int = Toast.LENGTH_SHORT,
    onShown: (() -> Unit)? = null
) {
    val context = LocalContext.current

    LaunchedEffect(message, show, duration) {
        if (show && message.isNotBlank()) {
            Toast.makeText(context, message, duration).show()
            onShown?.invoke()
        }
    }
}
