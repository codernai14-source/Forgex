package com.forgex.mobile.feature.auth.data

data class SliderCaptcha(
    val id: String,
    val backgroundImageBase64: String,
    val templateImageBase64: String,
    val backgroundImageWidth: Int,
    val backgroundImageHeight: Int,
    val templateImageWidth: Int,
    val templateImageHeight: Int
)
