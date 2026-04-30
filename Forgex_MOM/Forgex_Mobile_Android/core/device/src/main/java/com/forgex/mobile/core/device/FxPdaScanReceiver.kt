package com.forgex.mobile.core.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxScanSource

/**
 * PDA 扫码广播接收器。
 *
 * 该接收器只负责将厂商广播解析成统一扫描结果，不直接处理页面逻辑。
 *
 * @param onScan 扫描结果回调
 */
class FxPdaScanReceiver(
    private val onScan: (FxScanResult) -> Unit
) : BroadcastReceiver() {

    /**
     * 接收并解析 PDA 扫码广播。
     *
     * @param context 广播上下文
     * @param intent 广播 Intent
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val payload = extractPayload(intent?.extras) ?: return
        onScan(
            FxScanResult(
                rawValue = payload.value,
                source = FxScanSource.PDA_BROADCAST,
                deviceBrand = Build.BRAND,
                metadata = buildMap {
                    intent?.action?.takeIf { it.isNotBlank() }?.let { put("action", it) }
                    put("extraKey", payload.extraKey)
                }
            )
        )
    }

    /**
     * 从广播 extras 中提取实际扫码值。
     *
     * @param extras 广播扩展字段
     * @return 扫码值与来源字段；提取失败时返回 `null`
     */
    private fun extractPayload(extras: Bundle?): ScanPayload? {
        if (extras == null) {
            return null
        }

        preferredExtraKeys.firstNotNullOfOrNull { key ->
            readBundleValue(extras, key)?.let { value ->
                ScanPayload(value = value, extraKey = key)
            }
        }?.let { return it }

        return extras.keySet()
            .mapNotNull { key ->
                readBundleValue(extras, key)?.let { value ->
                    ScanPayload(value = value, extraKey = key)
                }
            }
            .maxByOrNull { payload ->
                payload.value.length + scoringKeywords.count { keyword ->
                    payload.extraKey.contains(keyword, ignoreCase = true)
                } * 100
            }
    }

    /**
     * 读取并解码 Bundle 中指定键的值。
     *
     * @param bundle 广播扩展字段
     * @param key 字段名
     * @return 解码后的文本；无法读取时返回 `null`
     */
    @Suppress("DEPRECATION")
    private fun readBundleValue(bundle: Bundle, key: String): String? {
        return decodeExtra(bundle.get(key))
    }

    /**
     * 将广播 extra 解码为可用文本。
     *
     * @param extra 原始 extra 值
     * @return 可用文本；无法作为扫码值使用时返回 `null`
     */
    private fun decodeExtra(extra: Any?): String? {
        val decoded = when (extra) {
            null -> return null
            is ByteArray -> extra.toString(Charsets.UTF_8)
            is CharArray -> String(extra)
            is CharSequence -> extra.toString()
            is Number -> extra.toString()
            is Array<*> -> extra.firstNotNullOfOrNull(::decodeExtra)
            is Boolean -> return null
            else -> extra.toString().takeUnless { it.startsWith("[B@") }
        }

        return decoded
            ?.trim('\u0000', ' ', '\n', '\r', '\t')
            ?.takeIf { it.isNotBlank() }
    }

    /**
     * 已解析出的广播扫码载荷。
     *
     * @param value 扫码原始值
     * @param extraKey 命中的广播 extra 字段名
     */
    private data class ScanPayload(
        val value: String,
        val extraKey: String
    )

    companion object {
        private val preferredExtraKeys = listOf(
            "data",
            "data_string",
            "scannerdata",
            "scanData",
            "scan_data",
            "barcode",
            "barcode_string",
            "barcodeString",
            "decode_data",
            "decodeData",
            "result",
            "text",
            "value"
        )

        private val scoringKeywords = listOf(
            "data",
            "scan",
            "barcode",
            "decode",
            "result",
            "text",
            "value"
        )
    }
}
