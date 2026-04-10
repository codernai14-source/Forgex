package com.forgex.mobile.core.network.model.auth

import com.google.gson.annotations.SerializedName

data class SliderCaptchaPayload(
    val id: String? = null,
    val type: String? = null,
    val backgroundImage: String? = null,
    @SerializedName(value = "templateImage", alternate = ["sliderImage"])
    val templateImage: String? = null,
    val backgroundImageWidth: Int? = null,
    val backgroundImageHeight: Int? = null,
    val templateImageWidth: Int? = null,
    val templateImageHeight: Int? = null
)

data class SliderValidateRequest(
    val id: String,
    val track: SliderTrackPayload
)

data class SliderTrackPayload(
    val bgImageWidth: Int,
    val bgImageHeight: Int,
    val templateImageWidth: Int,
    val templateImageHeight: Int,
    val startTime: Long,
    val stopTime: Long,
    val left: Int,
    val top: Int,
    val trackList: List<SliderTrackPointPayload>,
    val data: Any? = null
)

data class SliderTrackPointPayload(
    val x: Float,
    val y: Float,
    val t: Float,
    val type: String
)
