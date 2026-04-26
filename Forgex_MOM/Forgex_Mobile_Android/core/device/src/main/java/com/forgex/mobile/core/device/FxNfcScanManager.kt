package com.forgex.mobile.core.device

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxScanSource

/**
 * NFC 扫描管理器。
 *
 * 负责将系统 NFC Intent 转换为统一扫描结果，
 * 并为前台页面提供 NFC 前台分发能力。
 */
class FxNfcScanManager {

    /**
     * 判断当前设备是否支持 NFC。
     *
     * @param activity 当前 Activity
     * @return `true` 表示设备支持 NFC
     */
    fun isNfcSupported(activity: Activity): Boolean {
        return NfcAdapter.getDefaultAdapter(activity) != null
    }

    /**
     * 为当前 Activity 开启 NFC 前台分发。
     *
     * @param activity 当前前台 Activity
     */
    fun enableForegroundDispatch(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            pendingIntentFlags()
        )

        runCatching {
            adapter.enableForegroundDispatch(
                activity,
                pendingIntent,
                foregroundIntentFilters,
                null
            )
        }
    }

    /**
     * 关闭当前 Activity 的 NFC 前台分发。
     *
     * @param activity 当前 Activity
     */
    fun disableForegroundDispatch(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        runCatching {
            adapter.disableForegroundDispatch(activity)
        }
    }

    /**
     * 从系统 Intent 中解析 NFC 扫描结果。
     *
     * @param intent 系统分发的 NFC Intent
     * @return 统一扫描结果；当前 Intent 不是 NFC 事件时返回 `null`
     */
    fun parseIntent(intent: Intent?): FxScanResult? {
        val action = intent?.action ?: return null
        if (
            action != NfcAdapter.ACTION_TAG_DISCOVERED &&
            action != NfcAdapter.ACTION_TECH_DISCOVERED &&
            action != NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            return null
        }

        val tag = readTag(intent) ?: return null
        val rawValue = readNfcPayload(intent, tag).ifBlank { tag.id.toHexString() }
        if (rawValue.isBlank()) {
            return null
        }

        return FxScanResult(
            rawValue = rawValue,
            source = FxScanSource.NFC,
            deviceBrand = Build.BRAND,
            metadata = buildMap {
                put("action", action)
                put("tagId", tag.id.toHexString())
                put("techList", tag.techList.joinToString(","))
            }
        )
    }

    /**
     * 读取 NFC 标签中的文本内容，读取失败时回退为标签 ID。
     *
     * @param intent 系统 NFC Intent
     * @param tag 已解析出的 NFC 标签
     * @return 标签文本内容
     */
    private fun readNfcPayload(intent: Intent, tag: Tag): String {
        val payloadFromNdef = readNdefMessages(intent)
            .flatMap { it.records.toList() }
            .mapNotNull { record ->
                runCatching { decodeRecord(record.payload) }.getOrNull()
            }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        if (payloadFromNdef.isNotBlank()) {
            return payloadFromNdef
        }

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            return runCatching {
                ndef.connect()
                ndef.ndefMessage?.records
                    ?.mapNotNull { record -> decodeRecord(record.payload).takeIf { it.isNotBlank() } }
                    ?.firstOrNull()
                    .orEmpty()
            }.getOrDefault("").also {
                runCatching { ndef.close() }
            }
        }

        val formatable = NdefFormatable.get(tag)
        if (formatable != null) {
            return tag.id.toHexString()
        }
        return ""
    }

    /**
     * 解码 NFC 记录内容，优先按文本记录处理。
     *
     * @param payload NFC 记录原始字节数组
     * @return 解码后的文本
     */
    private fun decodeRecord(payload: ByteArray): String {
        if (payload.isEmpty()) {
            return ""
        }
        return runCatching {
            val languageCodeLength = payload.first().toInt() and 0x3F
            String(payload, 1 + languageCodeLength, payload.size - 1 - languageCodeLength, Charsets.UTF_8)
        }.getOrElse {
            String(payload, Charsets.UTF_8)
        }.trim()
    }

    /**
     * 兼容不同 Android 版本读取 NFC Tag。
     *
     * @param intent NFC Intent
     * @return NFC 标签对象
     */
    private fun readTag(intent: Intent): Tag? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag
        }
    }

    /**
     * 兼容不同 Android 版本读取 NDEF 消息数组。
     *
     * @param intent NFC Intent
     * @return NDEF 消息列表
     */
    private fun readNdefMessages(intent: Intent): List<NdefMessage> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES,
                NdefMessage::class.java
            )?.toList().orEmpty()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                ?.mapNotNull { it as? NdefMessage }
                .orEmpty()
        }
    }

    /**
     * 计算 NFC 前台分发所需的 PendingIntent 标记。
     *
     * @return PendingIntent 标记位
     */
    private fun pendingIntentFlags(): Int {
        val mutableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        return PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag
    }

    /**
     * 将标签 ID 转换为十六进制文本。
     *
     * @return 十六进制形式的标签 ID
     */
    private fun ByteArray.toHexString(): String {
        return joinToString(separator = "") { byte -> "%02X".format(byte) }
    }

    companion object {
        private val foregroundIntentFilters: Array<IntentFilter> by lazy {
            arrayOf(
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    runCatching { addDataType("*/*") }
                },
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                },
                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
            )
        }
    }
}
