package com.forgex.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.forgex.mobile.core.ui.device.rememberDeviceType
import com.forgex.mobile.feature.auth.AUTH_ROUTE
import com.forgex.mobile.feature.auth.AuthScreen
import com.forgex.mobile.feature.home.HOME_ROUTE
import com.forgex.mobile.feature.home.HomeScreen
import com.forgex.mobile.feature.message.MESSAGE_ROUTE
import com.forgex.mobile.feature.message.MessageScreen
import com.forgex.mobile.feature.profile.PROFILE_ROUTE
import com.forgex.mobile.feature.profile.ProfileScreen
import com.forgex.mobile.feature.workflow.WORKFLOW_ROUTE
import com.forgex.mobile.feature.workflow.WorkflowScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgexMobileApp() {
    val navController = rememberNavController()
    val deviceType = rememberDeviceType()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Forgex Mobile (${deviceType.name})")
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AUTH_ROUTE,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AUTH_ROUTE) {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate(HOME_ROUTE) {
                            popUpTo(AUTH_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            composable(HOME_ROUTE) {
                HomeScreen(
                    deviceType = deviceType,
                    onOpenWorkflow = { navController.navigate(WORKFLOW_ROUTE) },
                    onOpenMessage = { navController.navigate(MESSAGE_ROUTE) },
                    onOpenProfile = { navController.navigate(PROFILE_ROUTE) }
                )
            }
            composable(WORKFLOW_ROUTE) {
                WorkflowScreen(onBack = { navController.popBackStack() })
            }
            composable(MESSAGE_ROUTE) {
                MessageScreen(onBack = { navController.popBackStack() })
            }
            composable(PROFILE_ROUTE) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
