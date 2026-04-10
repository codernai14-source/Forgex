package com.forgex.mobile.feature.home

data class HomeMenuItem(
    val title: String,
    val path: String,
    val componentKey: String,
    val pageRenderType: String? = null,
    val menuMode: String? = null,
    val externalUrl: String? = null,
    val moduleCode: String? = null,
    val menuType: String? = null
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val menuItems: List<HomeMenuItem> = emptyList()
)
