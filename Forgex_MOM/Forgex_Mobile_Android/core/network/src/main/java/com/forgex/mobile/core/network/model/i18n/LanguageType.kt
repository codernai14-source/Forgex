package com.forgex.mobile.core.network.model.i18n

data class LanguageType(
    val id: Long? = null,
    val langCode: String = "",
    val langName: String = "",
    val langNameEn: String = "",
    val icon: String = "",
    val orderNum: Int? = null,
    val enabled: Boolean = false,
    val isDefault: Boolean = false
)
