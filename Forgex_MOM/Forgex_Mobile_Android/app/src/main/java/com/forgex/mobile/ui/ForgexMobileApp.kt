package com.forgex.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.forgex.mobile.core.ui.device.rememberDeviceType
import com.forgex.mobile.feature.auth.AUTH_ROUTE
import com.forgex.mobile.feature.auth.AuthScreen
import com.forgex.mobile.feature.home.HOME_ROUTE
import com.forgex.mobile.feature.home.HomeMenuItem
import com.forgex.mobile.feature.home.HomeScreen
import com.forgex.mobile.feature.message.MESSAGE_ROUTE
import com.forgex.mobile.feature.message.MessageScreen
import com.forgex.mobile.feature.profile.PROFILE_ROUTE
import com.forgex.mobile.feature.profile.ProfileScreen
import com.forgex.mobile.feature.workflow.WORKFLOW_ROUTE
import com.forgex.mobile.feature.workflow.WorkflowScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgexMobileApp() {
    val navController = rememberNavController()
    val deviceType = rememberDeviceType()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Forgex Mobile (${deviceType.name})")
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    onMenuClick = { item ->
                        val destination = resolveDestination(item)
                        if (destination == null) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("功能尚未接入: ${item.title}")
                            }
                        } else {
                            navController.navigate(destination)
                        }
                    }
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

private fun resolveDestination(item: HomeMenuItem): String? {
    val key = item.componentKey.lowercase()
    val path = item.path.lowercase()

    return when {
        key.contains("workflow") || path.contains("workflow") || path.contains("approve") -> WORKFLOW_ROUTE
        key.contains("message") || path.contains("message") -> MESSAGE_ROUTE
        key.contains("profile") || path.contains("personal") || path.contains("mine") -> PROFILE_ROUTE
        key.contains("home") || path == "home" -> HOME_ROUTE
        else -> null
    }
}
