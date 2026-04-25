package com.forgex.mobile.feature.integration.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.integration.presentation.IntegrationPlaceholderScreen

fun NavGraphBuilder.integrationScreen() {
    composable(INTEGRATION_ROUTE) {
        IntegrationPlaceholderScreen()
    }
}
