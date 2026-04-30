package com.forgex.mobile.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.forgex.mobile.R
import com.forgex.mobile.core.ui.i18n.ProvideI18nBundle
import com.forgex.mobile.core.ui.i18n.i18nString
import com.forgex.mobile.core.ui.device.rememberDeviceType
import com.forgex.mobile.feature.auth.AUTH_ROUTE
import com.forgex.mobile.feature.auth.AuthScreen
import com.forgex.mobile.feature.auth.REGISTER_ROUTE
import com.forgex.mobile.feature.auth.RegisterPlaceholderScreen
import com.forgex.mobile.feature.auth.SERVER_SETTINGS_ROUTE
import com.forgex.mobile.feature.auth.ServerSettingsScreen
import com.forgex.mobile.feature.basic.navigation.basicScreen
import com.forgex.mobile.feature.home.HOME_ROUTE
import com.forgex.mobile.feature.home.HomeScreen
import com.forgex.mobile.feature.home.BASIC_INFO_TEST_ROUTE
import com.forgex.mobile.feature.home.BasicInfoTestScreen
import com.forgex.mobile.feature.integration.navigation.integrationScreen
import com.forgex.mobile.feature.label.navigation.labelScreen
import com.forgex.mobile.feature.message.MESSAGE_READ_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_UNREAD_ROUTE
import com.forgex.mobile.feature.message.MessageEntryMode
import com.forgex.mobile.feature.message.MessageScreen
import com.forgex.mobile.feature.production.navigation.productionScreen
import com.forgex.mobile.feature.profile.PROFILE_ROUTE
import com.forgex.mobile.feature.profile.ProfileScreen
import com.forgex.mobile.feature.quality.navigation.qualityScreen
import com.forgex.mobile.feature.report.navigation.reportScreen
import com.forgex.mobile.feature.warehouse.navigation.warehouseScreen
import com.forgex.mobile.feature.workflow.WORKFLOW_APPROVED_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_MINE_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_PENDING_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_ROUTE
import com.forgex.mobile.feature.workflow.WorkflowEntryMode
import com.forgex.mobile.feature.workflow.WorkflowScreen
import com.forgex.mobile.feature.equipment.navigation.equipmentScreen
import com.forgex.mobile.ui.navigation.MenuTargetResolver
import com.forgex.mobile.ui.navigation.MenuTargetType
import com.forgex.mobile.ui.navigation.WebViewDestination
import com.forgex.mobile.ui.webview.MobileWebViewScreen
import kotlinx.coroutines.launch

private const val MAIN_SHELL_ROUTE = "main_shell"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgexMobileApp() {
    val navController = rememberNavController()
    val deviceType = rememberDeviceType()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val shellViewModel: AppShellViewModel = hiltViewModel()
    val shellState by shellViewModel.uiState.collectAsState()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route.orEmpty()

    val showGlobalTopBar = currentRoute !in setOf(
        AUTH_ROUTE,
        SERVER_SETTINGS_ROUTE,
        REGISTER_ROUTE,
        MAIN_SHELL_ROUTE
    ) && !currentRoute.startsWith(WebViewDestination.ROUTE)

    ProvideI18nBundle(bundle = shellState.languageState.bundle) {
        val authRegisterLabel = i18nString("auth.register", R.string.auth_register)
        val commonNotAvailable = i18nString("common.not.available", R.string.common_not_available)
        val commonUrlMissing = i18nString("common.url.missing", R.string.common_url_missing)
        Scaffold(
            topBar = {
                if (showGlobalTopBar) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(
                                    R.string.common_device_title,
                                    shellState.systemName,
                                    deviceType.name
                                )
                            )
                        }
                    )
                }
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
                            navController.navigate(MAIN_SHELL_ROUTE) {
                                popUpTo(AUTH_ROUTE) { inclusive = true }
                            }
                        },
                        onShowMessage = { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        onOpenServerSettings = {
                            navController.navigate(SERVER_SETTINGS_ROUTE) { launchSingleTop = true }
                        },
                        onOpenRegister = { registerUrl ->
                            val resolvedUrl = registerUrl.trim()
                            if (resolvedUrl.startsWith("http://") || resolvedUrl.startsWith("https://")) {
                                navController.navigate(
                                    WebViewDestination.buildRoute(authRegisterLabel, resolvedUrl)
                                ) { launchSingleTop = true }
                            } else {
                                navController.navigate(REGISTER_ROUTE) { launchSingleTop = true }
                            }
                        }
                    )
                }
                composable(SERVER_SETTINGS_ROUTE) {
                    ServerSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(REGISTER_ROUTE) {
                    RegisterPlaceholderScreen(onBack = { navController.popBackStack() })
                }

                composable(MAIN_SHELL_ROUTE) {
                    MainShellScreen(
                        onMenuClick = { cMenu ->
                            val target = MenuTargetResolver.resolve(cMenu)
                            when (target.type) {
                                MenuTargetType.NATIVE -> {
                                    val destination = target.nativeRoute
                                    if (destination.isNullOrBlank()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "$commonNotAvailable: ${cMenu.name.orEmpty()}"
                                            )
                                        }
                                    } else {
                                        navController.navigate(destination) { launchSingleTop = true }
                                    }
                                }
                                MenuTargetType.WEBVIEW -> {
                                    val webUrl = target.webUrl
                                    if (webUrl.isNullOrBlank()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "$commonUrlMissing: ${cMenu.name.orEmpty()}"
                                            )
                                        }
                                    } else {
                                        navController.navigate(
                                            WebViewDestination.buildRoute(cMenu.name.orEmpty(), webUrl)
                                        ) { launchSingleTop = true }
                                    }
                                }
                                MenuTargetType.UNSUPPORTED -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            target.reason ?: commonNotAvailable
                                        )
                                    }
                                }
                            }
                        },
                        onLogout = {
                            navController.navigate(AUTH_ROUTE) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(HOME_ROUTE) {
                    HomeScreen(
                        deviceType = deviceType,
                        onMenuClick = { item ->
                            val target = MenuTargetResolver.resolve(item)
                            when (target.type) {
                                MenuTargetType.NATIVE -> {
                                    val destination = target.nativeRoute
                                    if (destination.isNullOrBlank()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "$commonNotAvailable: ${item.title}"
                                            )
                                        }
                                    } else {
                                        navController.navigate(destination) { launchSingleTop = true }
                                    }
                                }
                                MenuTargetType.WEBVIEW -> {
                                    val webUrl = target.webUrl
                                    if (webUrl.isNullOrBlank()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "$commonUrlMissing: ${item.title}"
                                            )
                                        }
                                    } else {
                                        navController.navigate(WebViewDestination.buildRoute(item.title, webUrl)) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                                MenuTargetType.UNSUPPORTED -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            target.reason ?: commonNotAvailable
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                composable(BASIC_INFO_TEST_ROUTE) {
                    BasicInfoTestScreen()
                }
                basicScreen()
                reportScreen()
                integrationScreen()
                warehouseScreen()
                productionScreen()
                qualityScreen()
                equipmentScreen()
                labelScreen()

                composable(WORKFLOW_ROUTE) {
                    WorkflowScreen(
                        entryMode = WorkflowEntryMode.HOME,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(WORKFLOW_PENDING_ROUTE) {
                    WorkflowScreen(
                        entryMode = WorkflowEntryMode.PENDING,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(WORKFLOW_APPROVED_ROUTE) {
                    WorkflowScreen(
                        entryMode = WorkflowEntryMode.APPROVED,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(WORKFLOW_MINE_ROUTE) {
                    WorkflowScreen(
                        entryMode = WorkflowEntryMode.MINE,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(MESSAGE_ROUTE) {
                    MessageScreen(
                        entryMode = MessageEntryMode.HOME,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(MESSAGE_UNREAD_ROUTE) {
                    MessageScreen(
                        entryMode = MessageEntryMode.UNREAD,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(MESSAGE_READ_ROUTE) {
                    MessageScreen(
                        entryMode = MessageEntryMode.READ,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(PROFILE_ROUTE) {
                    ProfileScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route = WebViewDestination.ROUTE_PATTERN,
                    arguments = listOf(
                        navArgument(WebViewDestination.ARG_TITLE) {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument(WebViewDestination.ARG_URL) {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val encodedTitle = backStackEntry.arguments
                        ?.getString(WebViewDestination.ARG_TITLE).orEmpty()
                    val encodedUrl = backStackEntry.arguments
                        ?.getString(WebViewDestination.ARG_URL).orEmpty()
                    MobileWebViewScreen(
                        title = Uri.decode(encodedTitle),
                        url = Uri.decode(encodedUrl),
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
