package com.forgex.mobile.feature.equipment.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.equipment.presentation.EquipmentPlaceholderScreen

fun NavGraphBuilder.equipmentScreen() {
    composable(EQUIPMENT_ROUTE) {
        EquipmentPlaceholderScreen()
    }
}
