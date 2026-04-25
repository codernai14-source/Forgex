package com.forgex.mobile.feature.warehouse.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.warehouse.presentation.WarehousePlaceholderScreen

fun NavGraphBuilder.warehouseScreen() {
    composable(WAREHOUSE_ROUTE) {
        WarehousePlaceholderScreen()
    }
}
