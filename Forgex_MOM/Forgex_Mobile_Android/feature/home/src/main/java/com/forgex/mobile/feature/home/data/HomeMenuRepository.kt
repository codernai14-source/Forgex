package com.forgex.mobile.feature.home.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.api.MenuApi
import com.forgex.mobile.core.network.model.menu.RouteNodeVO
import com.forgex.mobile.core.network.model.menu.RoutesRequest
import com.forgex.mobile.feature.home.HomeMenuItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class HomeMenuRepository @Inject constructor(
    private val menuApi: MenuApi,
    private val sessionStore: SessionStore
) {

    suspend fun loadHomeMenus(): AppResult<List<HomeMenuItem>> {
        val account = sessionStore.account.first()
        if (account.isNullOrBlank()) {
            return AppResult.Error("未获取到登录账号，请重新登录")
        }

        return try {
            val response = menuApi.getRoutes(RoutesRequest(account))
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }

            val routes = response.data?.routes.orEmpty()
            val menuItems = flattenRouteMenus(routes)

            if (menuItems.isEmpty()) {
                AppResult.Error("未加载到可用菜单")
            } else {
                AppResult.Success(menuItems)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载首页菜单失败")
        }
    }

    private fun flattenRouteMenus(nodes: List<RouteNodeVO>): List<HomeMenuItem> {
        val result = mutableListOf<HomeMenuItem>()

        fun walk(node: RouteNodeVO, inheritedModule: String?) {
            val moduleCode = node.meta?.module ?: inheritedModule
            val menuType = node.meta?.type
            val path = node.path.orEmpty()
            val title = node.meta?.title ?: node.name ?: path
            val componentKey = node.component.orEmpty()
            val pageRenderType = node.meta?.pageRenderType ?: node.pageRenderType
            val menuMode = node.meta?.menuMode ?: node.menuMode
            val externalUrl = node.meta?.externalUrl ?: node.externalUrl

            val isMenu = menuType.equals("menu", ignoreCase = true)
            if (isMenu && path.isNotBlank()) {
                result.add(
                    HomeMenuItem(
                        title = title,
                        path = path,
                        componentKey = componentKey,
                        pageRenderType = pageRenderType,
                        menuMode = menuMode,
                        externalUrl = externalUrl,
                        moduleCode = moduleCode,
                        menuType = menuType
                    )
                )
            }

            node.children.orEmpty().forEach { child ->
                walk(child, moduleCode)
            }
        }

        nodes.forEach { node -> walk(node, null) }
        return result.distinctBy { "${it.path}|${it.componentKey}|${it.title}" }
    }
}
