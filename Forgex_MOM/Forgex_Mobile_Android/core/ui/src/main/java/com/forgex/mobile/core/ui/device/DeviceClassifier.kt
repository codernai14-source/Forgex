package com.forgex.mobile.core.ui.device

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberDeviceType(tabletDpThreshold: Int = 600): DeviceType {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, tabletDpThreshold) {
        if (configuration.screenWidthDp >= tabletDpThreshold) DeviceType.TABLET else DeviceType.MOBILE
    }
}
