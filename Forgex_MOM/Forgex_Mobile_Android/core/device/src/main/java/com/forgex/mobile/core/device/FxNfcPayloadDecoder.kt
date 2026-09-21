package com.forgex.mobile.core.device

import android.nfc.NdefRecord
import java.nio.charset.Charset

/**
 * NDEF 记录解码器。文本记录需要先去掉状态字节和语言码，URI 记录则必须保留 URI 前缀。
 */
object FxNfcPayloadDecoder {

    fun decode(record: NdefRecord): String {
        if (record.payload.isEmpty()) return ""

        return when {
            record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                decodeText(record.payload)
            }
            record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI) -> {
                record.toUri()?.toString().orEmpty().ifBlank { decodeUri(record.payload) }
            }
            else -> decodeGeneric(record.payload)
        }.trim()
    }

    private fun decodeText(payload: ByteArray): String {
        val status = payload[0].toInt() and 0xFF
        val languageCodeLength = status and 0x3F
        val contentOffset = 1 + languageCodeLength
        if (contentOffset > payload.size) return ""
        val charset = if (status and 0x80 == 0) Charsets.UTF_8 else Charsets.UTF_16
        return payload.copyOfRange(contentOffset, payload.size).toString(charset)
    }

    private fun decodeUri(payload: ByteArray): String {
        if (payload.isEmpty()) return ""
        val prefix = uriPrefixes[payload[0].toInt() and 0xFF].orEmpty()
        return prefix + payload.copyOfRange(1, payload.size).toString(Charsets.UTF_8)
    }

    private fun decodeGeneric(payload: ByteArray): String {
        return payload.toString(Charsets.UTF_8)
    }

    private val uriPrefixes = mapOf(
        0x00 to "",
        0x01 to "http://www.",
        0x02 to "https://www.",
        0x03 to "http://",
        0x04 to "https://",
        0x05 to "tel:",
        0x06 to "mailto:",
        0x07 to "ftp://anonymous:anonymous@",
        0x08 to "ftp://ftp.",
        0x09 to "ftps://",
        0x0A to "sftp://",
        0x0B to "smb://",
        0x0C to "nfs://",
        0x0D to "ftp://",
        0x0E to "dav://",
        0x0F to "news:",
        0x10 to "telnet://",
        0x11 to "imap:",
        0x12 to "rtsp://",
        0x13 to "urn:",
        0x14 to "pop:",
        0x15 to "sip:",
        0x16 to "sips:",
        0x17 to "tftp:",
        0x18 to "btspp://",
        0x19 to "btl2cap://",
        0x1A to "btgoep://",
        0x1B to "tcpobex://",
        0x1C to "irdaobex://",
        0x1D to "file://",
        0x1E to "urn:epc:id:",
        0x1F to "urn:epc:tag:",
        0x20 to "urn:epc:pat:",
        0x21 to "urn:epc:raw:",
        0x22 to "urn:epc:",
        0x23 to "urn:nfc:"
    )
}
