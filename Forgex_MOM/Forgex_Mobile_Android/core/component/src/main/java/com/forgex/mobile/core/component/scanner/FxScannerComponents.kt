package com.forgex.mobile.core.component.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.component.FxTextField
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxScanSource
import com.forgex.mobile.core.ui.R

/**
 * 可消费外部扫描结果的输入框，统一支持手输、PDA 扫描和 NFC 回填。
 */
@Composable
fun FxScanInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    latestScanResult: FxScanResult? = null,
    onScanConsumed: (() -> Unit)? = null
) {
    var sourceLabelRes by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(latestScanResult?.timestamp) {
        val result = latestScanResult ?: return@LaunchedEffect
        onValueChange(result.rawValue)
        sourceLabelRes = result.source.displayNameRes()
        onScanConsumed?.invoke()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FxTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            enabled = enabled
        )
        if (sourceLabelRes != null) {
            Text(
                text = stringResource(
                    R.string.scan_source_hint,
                    stringResource(sourceLabelRes!!)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 扫描触发按钮，适用于 PDA/NFC 等外部设备引导提示。
 */
@Composable
fun FxScanActionButton(
    text: String = stringResource(R.string.scan_action_default),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(text)
    }
}

/**
 * 扫描结果提示弹窗。
 */
@Composable
fun FxScanResultDialog(
    result: FxScanResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.scan_result_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = result.rawValue,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.scan_result_source, result.source.displayName()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_confirm))
            }
        }
    )
}

/**
 * 可用于页面底部的扫描操作区。
 */
@Composable
fun FxScanActionBar(
    hint: String,
    actionText: String = stringResource(R.string.scan_action_default),
    enabled: Boolean = true,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        FxScanActionButton(
            text = actionText,
            enabled = enabled,
            onClick = onActionClick
        )
    }
}

@Composable
private fun FxScanSource.displayName(): String {
    return stringResource(displayNameRes())
}

private fun FxScanSource.displayNameRes(): Int {
    return when (this) {
        FxScanSource.PDA_BROADCAST -> R.string.scan_source_pda
        FxScanSource.HARDWARE_KEYBOARD -> R.string.scan_source_keyboard
        FxScanSource.NFC -> R.string.scan_source_nfc
        FxScanSource.CAMERA -> R.string.scan_source_camera
        FxScanSource.MANUAL_INPUT -> R.string.scan_source_manual
    }
}
