package com.forgex.mobile.ui.navigation

import android.net.Uri
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.feature.home.HOME_ROUTE
import com.forgex.mobile.feature.home.HomeMenuItem
import com.forgex.mobile.feature.message.MESSAGE_READ_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_ROUTE
import com.forgex.mobile.feature.message.MESSAGE_UNREAD_ROUTE
import com.forgex.mobile.feature.profile.PROFILE_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_APPROVED_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_MINE_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_PENDING_ROUTE
import com.forgex.mobile.feature.workflow.WORKFLOW_ROUTE

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

    private val componentKeyRouteMap = mapOf(
        "homescreen" to HOME_ROUTE,
        "workflowscreen" to WORKFLOW_ROUTE,
        "pendingtasklist" to WORKFLOW_PENDING_ROUTE,
        "approvedtasklist" to WORKFLOW_APPROVED_ROUTE,
        "mytasklist" to WORKFLOW_MINE_ROUTE,
        "messagescreen" to MESSAGE_ROUTE,
        "unreadmessagelist" to MESSAGE_UNREAD_ROUTE,
        "readmessagelist" to MESSAGE_READ_ROUTE,
        "profilescreen" to PROFILE_ROUTE
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
                MenuTarget(
                    type = MenuTargetType.UNSUPPORTED,
                    reason = "菜单已配置为 WEBVIEW，但缺少可访问 URL"
                )
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
            reason = if (renderType == "NATIVE") {
                "菜单声明为 NATIVE，但未匹配到原生页面"
            } else {
                "未匹配到菜单映射规则"
            }
        )
    }

    /**
     * 从 C 端菜单 VO 解析导航目标（工作台/收藏点击使用）。
     */
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
                MenuTarget(type = MenuTargetType.UNSUPPORTED, reason = "菜单配置为外联，但缺少 URL")
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

        return MenuTarget(type = MenuTargetType.UNSUPPORTED, reason = "未匹配到菜单映射规则")
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
