package com.forgex.mobile.core.component.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.ui.R

/**
 * NFC 状态提示行。
 *
 * 支持但未开启时展示引导文案并支持点击跳转系统设置；不支持时降级为灰字提示。
 * 设备状态的探测由调用方通过 [com.forgex.mobile.core.device.FxNfcScanManager] 完成。
 *
 * @param supported 设备是否支持 NFC
 * @param enabled 设备 NFC 是否已开启
 * @param onOpenSettings 点击跳转系统 NFC 设置
 * @param modifier 组件修饰符
 */
@Composable
fun FxNfcStatusHint(
    supported: Boolean,
    enabled: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, textRes, color) = when {
        !supported -> Triple(
            Icons.Outlined.ReportProblem as ImageVector,
            R.string.nfc_status_unsupported,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        !enabled -> Triple(
            Icons.Outlined.ReportProblem,
            R.string.nfc_status_disabled,
            MaterialTheme.colorScheme.error
        )
        else -> Triple(
            Icons.Outlined.Nfc,
            R.string.nfc_status_ready,
            MaterialTheme.colorScheme.primary
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (supported && !enabled) {
                    Modifier.clickable(onClick = onOpenSettings)
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
