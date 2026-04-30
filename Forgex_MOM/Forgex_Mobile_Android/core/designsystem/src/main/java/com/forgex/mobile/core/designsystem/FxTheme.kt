package com.forgex.mobile.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ForgexColorScheme = lightColorScheme(
    primary = FxColors.Primary,
    onPrimary = FxColors.Surface,
    primaryContainer = FxColors.PrimaryContainer,
    secondary = FxColors.Secondary,
    background = FxColors.Background,
    surface = FxColors.Surface,
    error = FxColors.Error,
    outline = FxColors.Border
)

private val ForgexTypography = Typography()

@Composable
fun FxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ForgexColorScheme,
        typography = ForgexTypography,
        content = content
    )
}
