package com.forgex.mobile.feature.production.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.production.presentation.ProductionPlaceholderScreen

fun NavGraphBuilder.productionScreen() {
    composable(PRODUCTION_ROUTE) {
        ProductionPlaceholderScreen()
    }
}
