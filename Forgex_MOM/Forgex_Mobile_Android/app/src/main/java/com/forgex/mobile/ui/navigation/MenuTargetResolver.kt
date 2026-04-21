package com.forgex.mobile.ui.navigation

import android.net.Uri
import com.forgex.mobile.feature.home.HOME_ROUTE
import com.forgex.mobile.feature.home.HomeMenuItem
import com.forgex.mobile.feature.home.BASIC_INFO_TEST_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_READ_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_UNREAD_ROUTE
import com.forgex.mobile.feature.profile.PROFILE_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_APPROVED_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_MINE_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_PENDING_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_ROUTE
import com.forgex.mobile.core.network.model.workbench.CMenuVO

enum class MenuTargetType {
    NATIVE,
    WEBVIEW,
    UNSUPPORTED
}

data class MenuTarget(
    val type: MenuTargetType,
    val nativeRoute: String? = null,
    val webUrl: String? = null,
    val reason: String? = null
)

object MenuTargetResolver {

    private const val WEBVIEW_URL_MISSING = "WEBVIEW is configured but no accessible URL was provided"
    private const val NATIVE_ROUTE_MISSING = "NATIVE is declared but no native screen matches the route"
    private const val MENU_RULE_MISSING = "No menu mapping rule matched"
    private const val EXTERNAL_URL_MISSING = "External menu is configured but URL is missing"

    private val componentKeyRouteMap = mapOf(
        "homescreen" to HOME_ROUTE,
        "workflowscreen" to WORKFLOW_ROUTE,
        "pendingtasklist" to WORKFLOW_PENDING_ROUTE,
        "approvedtasklist" to WORKFLOW_APPROVED_ROUTE,
        "mytasklist" to WORKFLOW_MINE_ROUTE,
        "messagescreen" to MESSAGE_ROUTE,
        "unreadmessagelist" to MESSAGE_UNREAD_ROUTE,
        "readmessagelist" to MESSAGE_READ_ROUTE,
        "profilescreen" to PROFILE_ROUTE,
        "basicinfotestscreen" to BASIC_INFO_TEST_ROUTE
    )

    fun resolve(item: HomeMenuItem): MenuTarget {
        val componentKey = item.componentKey.trim().lowercase()
        val path = item.path.trim()
        val normalizedPath = path.lowercase()
        val renderType = item.pageRenderType?.trim()?.uppercase().orEmpty()
        val menuMode = item.menuMode?.trim()?.lowercase().orEmpty()
        val externalUrl = item.externalUrl?.trim().orEmpty()

        if (renderType == "WEBVIEW" || menuMode == "external") {
            val url = when {
                externalUrl.isNotBlank() -> externalUrl
                path.isWebUrl() -> path
                else -> ""
            }
            return if (url.isNotBlank()) {
                MenuTarget(type = MenuTargetType.WEBVIEW, webUrl = url)
            } else {
                MenuTarget(type = MenuTargetType.UNSUPPORTED, reason = WEBVIEW_URL_MISSING)
            }
        }

        componentKeyRouteMap[componentKey]?.let { route ->
            return MenuTarget(type = MenuTargetType.NATIVE, nativeRoute = route)
        }

        resolvePathRoute(normalizedPath)?.let { route ->
            return MenuTarget(type = MenuTargetType.NATIVE, nativeRoute = route)
        }

        if (path.isWebUrl()) {
            return MenuTarget(type = MenuTargetType.WEBVIEW, webUrl = path)
        }

        return MenuTarget(
            type = MenuTargetType.UNSUPPORTED,
            reason = if (renderType == "NATIVE") NATIVE_ROUTE_MISSING else MENU_RULE_MISSING
        )
    }

    fun resolve(menu: CMenuVO): MenuTarget {
        val componentKey = menu.componentKey?.trim()?.lowercase().orEmpty()
        val path = menu.path?.trim().orEmpty()
        val normalizedPath = path.lowercase()
        val menuMode = menu.menuMode?.trim()?.lowercase().orEmpty()
        val externalUrl = menu.externalUrl?.trim().orEmpty()

        if (menuMode == "external") {
            val url = externalUrl.ifBlank { path }.takeIf { it.isWebUrl() }.orEmpty()
            return if (url.isNotBlank()) {
                MenuTarget(type = MenuTargetType.WEBVIEW, webUrl = url)
            } else {
                MenuTarget(type = MenuTargetType.UNSUPPORTED, reason = EXTERNAL_URL_MISSING)
            }
        }

        componentKeyRouteMap[componentKey]?.let { route ->
            return MenuTarget(type = MenuTargetType.NATIVE, nativeRoute = route)
        }

        resolvePathRoute(normalizedPath)?.let { route ->
            return MenuTarget(type = MenuTargetType.NATIVE, nativeRoute = route)
        }

        if (path.isWebUrl()) {
            return MenuTarget(type = MenuTargetType.WEBVIEW, webUrl = path)
        }

        return MenuTarget(type = MenuTargetType.UNSUPPORTED, reason = MENU_RULE_MISSING)
    }

    private fun resolvePathRoute(path: String): String? {
        if (path == "home" || path == "/home" || path.endsWith("/home")) {
            return HOME_ROUTE
        }

        if (path.contains("message")) {
            return when {
                path.contains("unread") -> MESSAGE_UNREAD_ROUTE
                path.contains("read") -> MESSAGE_READ_ROUTE
                else -> MESSAGE_ROUTE
            }
        }

        if (path.contains("workflow") || path.contains("approve") || path.contains("task")) {
            return when {
                path.contains("pending") -> WORKFLOW_PENDING_ROUTE
                path.contains("approved") || path.contains("processed") -> WORKFLOW_APPROVED_ROUTE
                path.contains("mine") || path.contains("initiated") -> WORKFLOW_MINE_ROUTE
                else -> WORKFLOW_ROUTE
            }
        }

        if (path.contains("profile") || path.contains("personal")) {
            return PROFILE_ROUTE
        }

        if (path.contains("basic/info-test") || path.contains("basicinfotest")) {
            return BASIC_INFO_TEST_ROUTE
        }

        return null
    }
}

object WebViewDestination {
    const val ROUTE = "webview"
    const val ARG_TITLE = "title"
    const val ARG_URL = "url"
    const val ROUTE_PATTERN = "$ROUTE?$ARG_TITLE={$ARG_TITLE}&$ARG_URL={$ARG_URL}"

    fun buildRoute(title: String, url: String): String {
        return "$ROUTE?$ARG_TITLE=${Uri.encode(title)}&$ARG_URL=${Uri.encode(url)}"
    }
}

private fun String.isWebUrl(): Boolean {
    return startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
}
