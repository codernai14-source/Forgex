package com.forgex.mobile.core.device

import android.content.Intent
import android.os.Bundle
import com.forgex.mobile.core.model.FxScanSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FxPdaScanReceiverTest {

    @Test
    fun readsCommonZebraDataStringExtra() {
        var result: com.forgex.mobile.core.model.FxScanResult? = null
        val receiver = FxPdaScanReceiver { result = it }
        val intent = Intent("com.symbol.datawedge.api.RESULT_ACTION")
            .putExtra("com.symbol.datawedge.data_string", "  690123  \n")

        receiver.onReceive(null, intent)

        assertNotNull(result)
        assertEquals("690123", result?.rawValue)
        assertEquals(FxScanSource.PDA_BROADCAST, result?.source)
    }

    @Test
    fun ignoresBooleanAndEmptyExtras() {
        var result: com.forgex.mobile.core.model.FxScanResult? = null
        val receiver = FxPdaScanReceiver { result = it }
        val extras = Bundle().apply {
            putBoolean("decode_complete", true)
            putString("data", "   ")
        }
        val intent = Intent("com.seuic.scanner.decode").putExtras(extras)

        receiver.onReceive(null, intent)

        assertEquals(null, result)
    }
}
