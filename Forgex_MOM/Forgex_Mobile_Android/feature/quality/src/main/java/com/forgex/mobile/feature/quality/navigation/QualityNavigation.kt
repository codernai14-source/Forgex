package com.forgex.mobile.feature.quality.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.quality.presentation.QualityPlaceholderScreen

fun NavGraphBuilder.qualityScreen() {
    composable(QUALITY_ROUTE) {
        QualityPlaceholderScreen()
    }
}
