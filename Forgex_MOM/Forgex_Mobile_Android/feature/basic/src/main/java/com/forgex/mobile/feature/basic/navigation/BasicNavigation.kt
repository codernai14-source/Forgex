package com.forgex.mobile.feature.basic.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.basic.presentation.BasicPlaceholderScreen

fun NavGraphBuilder.basicScreen() {
    composable(BASIC_ROUTE) {
        BasicPlaceholderScreen()
    }
}
