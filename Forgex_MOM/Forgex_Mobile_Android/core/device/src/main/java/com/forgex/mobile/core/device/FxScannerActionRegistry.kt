package com.forgex.mobile.core.device

/**
 * PDA 扫码广播 action 注册表。
 *
 * 该注册表用于根据设备品牌返回优先匹配的广播 action，
 * 同时追加一组通用兜底 action，避免因品牌识别不准确导致无法接收扫码结果。
 *
 * @param mappings 品牌到广播 action 列表的映射关系
 */
class FxScannerActionRegistry(
    private val mappings: Map<String, List<String>> = defaultMappings
) {

    /**
     * 根据设备品牌返回可监听的 PDA 扫码广播 action 列表。
     *
     * @param brand 设备品牌
     * @return 当前品牌优先 action 与通用兜底 action 的去重结果
     */
    fun actionsForBrand(brand: String?): List<String> {
        val normalizedBrand = brand?.trim()?.lowercase().orEmpty()
        val brandMatchedActions = mappings.entries
            .filter { normalizedBrand.contains(it.key) }
            .flatMap { it.value }

        return (brandMatchedActions + fallbackActions).distinct()
    }

    companion object {
        private val defaultMappings = mapOf(
            "honeywell" to listOf("com.honeywell.decode.intent.action"),
            "seuic" to listOf("com.seuic.scanner.decode"),
            "urovo" to listOf("android.intent.ACTION_DECODE_DATA")
        )

        private val fallbackActions = defaultMappings.values.flatten().distinct()
    }
}
