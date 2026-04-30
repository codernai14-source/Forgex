package com.forgex.mobile.feature.label.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.label.presentation.LabelPlaceholderScreen

fun NavGraphBuilder.labelScreen() {
    composable(LABEL_ROUTE) {
        LabelPlaceholderScreen()
    }
}
