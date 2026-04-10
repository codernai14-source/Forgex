package com.forgex.mobile.core.network.model.menu

data class UserRoutesVO(
    val modules: List<Map<String, Any?>> = emptyList(),
    val routes: List<Map<String, Any?>> = emptyList(),
    val buttons: List<String> = emptyList()
)
