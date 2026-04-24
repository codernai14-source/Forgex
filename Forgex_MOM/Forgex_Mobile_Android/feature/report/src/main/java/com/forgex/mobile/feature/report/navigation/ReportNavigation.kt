package com.forgex.mobile.feature.report.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forgex.mobile.feature.report.presentation.ReportPlaceholderScreen

fun NavGraphBuilder.reportScreen() {
    composable(REPORT_ROUTE) {
        ReportPlaceholderScreen()
    }
}
