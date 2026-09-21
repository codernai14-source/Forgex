package com.forgex.mobile.core.device

import android.nfc.NdefRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FxNfcPayloadDecoderTest {

    @Test
    fun decodesUtf8TextRecordWithoutLanguageCode() {
        val payload = byteArrayOf(0x02) + "en".toByteArray() + "物料-001".toByteArray()
        val record = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            byteArrayOf(),
            payload
        )

        assertEquals("物料-001", FxNfcPayloadDecoder.decode(record))
    }

    @Test
    fun decodesUtf16TextRecord() {
        val payload = byteArrayOf(0x82.toByte()) + "en".toByteArray() + "仓库".toByteArray(Charsets.UTF_16)
        val record = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            byteArrayOf(),
            payload
        )

        assertEquals("仓库", FxNfcPayloadDecoder.decode(record))
    }

    @Test
    fun decodesUriRecordWithWellKnownPrefix() {
        val record = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_URI,
            byteArrayOf(),
            byteArrayOf(0x01) + "example.com/item/1".toByteArray()
        )

        assertEquals("http://www.example.com/item/1", FxNfcPayloadDecoder.decode(record))
    }
}
