package com.forgex.mobile.core.network.model.menu

data class UserRoutesVO(
    val modules: List<Map<String, Any?>> = emptyList(),
    val routes: List<RouteNodeVO> = emptyList(),
    val buttons: List<String> = emptyList()
)

data class RouteNodeVO(
    val path: String? = null,
    val name: String? = null,
    val component: String? = null,
    val menuMode: String? = null,
    val externalUrl: String? = null,
    val pageRenderType: String? = null,
    val meta: RouteMetaVO? = null,
    val children: List<RouteNodeVO>? = null
)

data class RouteMetaVO(
    val title: String? = null,
    val icon: String? = null,
    val module: String? = null,
    val menuLevel: Int? = null,
    val type: String? = null,
    val perms: List<String>? = null,
    val menuMode: String? = null,
    val externalUrl: String? = null,
    val pageRenderType: String? = null
)
