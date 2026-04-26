package com.forgex.mobile.core.model

/**
 * 扫码来源定义。
 */
enum class FxScanSource {
    PDA_BROADCAST,
    HARDWARE_KEYBOARD,
    NFC,
    CAMERA,
    MANUAL_INPUT
}

/**
 * 扫码结果统一协议。
 */
data class FxScanResult(
    val rawValue: String,
    val type: String? = null,
    val source: FxScanSource,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceBrand: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * 扫码行为配置。
 */
data class FxScannerConfig(
    val debounceMillis: Long = 300L,
    val allowContinuousScan: Boolean = true,
    val autoClearInput: Boolean = true,
    val vibrateOnSuccess: Boolean = false,
    val beepOnSuccess: Boolean = false
)
