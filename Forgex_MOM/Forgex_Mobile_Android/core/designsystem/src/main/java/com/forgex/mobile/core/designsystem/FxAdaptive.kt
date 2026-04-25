package com.forgex.mobile.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.forgex.mobile.core.ui.device.DeviceType
import com.forgex.mobile.core.ui.device.rememberDeviceType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FxAdaptiveMetrics(
    val contentPadding: Dp,
    val sectionSpacing: Dp,
    val formMaxWidth: Dp
)

@Composable
fun rememberFxAdaptiveMetrics(deviceType: DeviceType = rememberDeviceType()): FxAdaptiveMetrics {
    return when (deviceType) {
        DeviceType.TABLET -> FxAdaptiveMetrics(
            contentPadding = 24.dp,
            sectionSpacing = 20.dp,
            formMaxWidth = 720.dp
        )
        DeviceType.MOBILE -> FxAdaptiveMetrics(
            contentPadding = 16.dp,
            sectionSpacing = 16.dp,
            formMaxWidth = 560.dp
        )
    }
}
