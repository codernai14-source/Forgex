package com.forgex.mobile.core.network.model.auth

data class SystemBasicConfig(
    val systemName: String? = null,
    val systemLogo: String? = null,
    val loginPageTitle: String? = null,
    val loginPageSubtitle: String? = null,
    val loginBackgroundType: String? = null,
    val loginBackgroundVideo: String? = null,
    val loginBackgroundImage: String? = null,
    val loginBackgroundColor: String? = null,
    val loginStyle: String? = null,
    val loginLayout: String? = null,
    val showOAuthLogin: Boolean? = null,
    val showRegisterEntry: Boolean? = null,
    val registerUrl: String? = null,
    val primaryColor: String? = null,
    val secondaryColor: String? = null
)
