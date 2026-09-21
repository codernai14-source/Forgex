package com.forgex.mobile.core.device

import org.junit.Assert.assertEquals
import org.junit.Test

class FxCameraScanManagerTest {

    @Test
    fun choosesFirstNonEmptyBarcodeValue() {
        val values = listOf<String?>("", "  ", "ABC-001", "SECOND")

        assertEquals("ABC-001", FxCameraScanManager.firstValue(values))
    }
}
