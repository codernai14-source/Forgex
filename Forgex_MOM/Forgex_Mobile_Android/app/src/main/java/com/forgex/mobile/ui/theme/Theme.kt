package com.forgex.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryTeal,
    background = LightBackground
)

private val DarkScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryTeal,
    background = DarkBackground
)

@Composable
fun ForgexMobileTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        content = content
    )
}
